/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_ncnn_yolo.h"

#include "pretext_native_lock.h"

#include <algorithm>
#include <cmath>
#include <cstring>
#include <vector>

#include <ncnn/net.h>

namespace pretext_ncnn {
namespace {

constexpr int kInputSize = 640;
constexpr int kRegMax = 16;
constexpr int kNumClasses = 80;
constexpr int kDetFeatDim = 64 + kNumClasses;
constexpr int kMaskCoeffs = 32;
constexpr int kProtoSide = 160;
constexpr float kScoreThr = 0.22f;
constexpr float kScoreThrHigh = 0.18f;
constexpr float kNmsThr = 0.40f;
constexpr int kPersonClassId = 0;

enum class ModelKind { None, Seg };

ncnn::Net g_net;
ModelKind g_kind = ModelKind::None;
bool g_ready = false;
bool g_highQuality = false;

float scoreThreshold() {
    return g_highQuality ? kScoreThrHigh : kScoreThr;
}

void applyNetOptions() {
    ncnn::Option opt;
    opt.num_threads = g_highQuality ? 4 : 2;
    opt.use_vulkan_compute = false;
    g_net.opt = opt;
}

static const char* kCoco[] = {
    "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
    "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
    "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
    "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
    "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
    "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
    "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
    "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote",
    "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book",
    "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush",
};

struct Proposal {
    float x0 = 0.f;
    float y0 = 0.f;
    float x1 = 0.f;
    float y1 = 0.f;
    float score = 0.f;
    int label = 0;
    int anchor = 0;
    float maskCoeff[kMaskCoeffs]{};
};

struct Letterbox {
    float scale = 1.f;
    int padX = 0;
    int padY = 0;
    int wPad = 0;
    int hPad = 0;
};

static inline float sigmoid(float x) {
    return 1.f / (1.f + std::exp(-x));
}

static float decodeDfl(const float* logits, int regMax) {
    float maxL = logits[0];
    for (int i = 1; i < regMax; ++i) {
        maxL = std::max(maxL, logits[i]);
    }
    float sum = 0.f;
    float acc = 0.f;
    for (int i = 0; i < regMax; ++i) {
        const float e = std::exp(logits[i] - maxL);
        sum += e;
        acc += e * static_cast<float>(i);
    }
    return sum > 0.f ? acc / sum : 0.f;
}

static void gridCell(int anchorIndex, int& gx, int& gy, int& stride) {
    if (anchorIndex < 6400) {
        stride = 8;
        gx = anchorIndex % 80;
        gy = anchorIndex / 80;
    } else if (anchorIndex < 8000) {
        stride = 16;
        const int n = anchorIndex - 6400;
        gx = n % 40;
        gy = n / 40;
    } else {
        stride = 32;
        const int n = anchorIndex - 8000;
        gx = n % 20;
        gy = n / 20;
    }
}

static float iou(const Proposal& a, const Proposal& b) {
    const float interL = std::max(a.x0, b.x0);
    const float interT = std::max(a.y0, b.y0);
    const float interR = std::min(a.x1, b.x1);
    const float interB = std::min(a.y1, b.y1);
    const float iw = std::max(0.f, interR - interL);
    const float ih = std::max(0.f, interB - interT);
    const float inter = iw * ih;
    const float areaA = std::max(0.f, a.x1 - a.x0) * std::max(0.f, a.y1 - a.y0);
    const float areaB = std::max(0.f, b.x1 - b.x0) * std::max(0.f, b.y1 - b.y0);
    const float uni = areaA + areaB - inter;
    return uni > 0.f ? inter / uni : 0.f;
}

static void nms(std::vector<Proposal>& props, float thr) {
    std::sort(props.begin(), props.end(), [](const Proposal& a, const Proposal& b) {
        return a.score > b.score;
    });
    std::vector<Proposal> kept;
    for (const auto& p : props) {
        bool drop = false;
        for (const auto& k : kept) {
            if (iou(p, k) > thr) {
                drop = true;
                break;
            }
        }
        if (!drop) kept.push_back(p);
    }
    props.swap(kept);
}

static void readMaskCoeffFromOut1(
    const ncnn::Mat& out1,
    int anchor,
    float* coeffOut) {
    if (out1.h == kMaskCoeffs && out1.w > anchor) {
        for (int c = 0; c < kMaskCoeffs; ++c) {
            coeffOut[c] = out1.row(c)[anchor];
        }
    } else if (out1.w == kMaskCoeffs && out1.h > anchor) {
        const float* row = out1.row(anchor);
        for (int c = 0; c < kMaskCoeffs; ++c) {
            coeffOut[c] = row[c];
        }
    }
}

static void generateProposalsYolov8(
    const ncnn::Mat& feat,
    const ncnn::Mat* out1,
    float scale,
    int padX,
    int padY,
    int imgW,
    int imgH,
    bool excludePerson,
    std::vector<Proposal>& props) {
    const int numAnchors = feat.w;
    const int channels = feat.h;
    if (numAnchors <= 0 || channels < kDetFeatDim) return;

    auto dflSide = [&](int anchor, int side) {
        float logits[kRegMax];
        const int base = side * kRegMax;
        for (int r = 0; r < kRegMax; ++r) {
            logits[r] = feat.row(base + r)[anchor];
        }
        return decodeDfl(logits, kRegMax);
    };

    for (int i = 0; i < numAnchors; ++i) {
        int gx = 0;
        int gy = 0;
        int stride = 8;
        gridCell(i, gx, gy, stride);

        const float left = dflSide(i, 0);
        const float top = dflSide(i, 1);
        const float right = dflSide(i, 2);
        const float bottom = dflSide(i, 3);

        int bestLabel = 0;
        float bestLogit = -1e9f;
        for (int c = 0; c < kNumClasses; ++c) {
            const float logit = feat.row(64 + c)[i];
            if (logit > bestLogit) {
                bestLogit = logit;
                bestLabel = c;
            }
        }
        const float score = sigmoid(bestLogit);
        if (score < scoreThreshold()) continue;
        if (excludePerson && bestLabel == kPersonClassId) continue;

        const float cx0 = (static_cast<float>(gx) + 0.5f - left) * static_cast<float>(stride);
        const float cy0 = (static_cast<float>(gy) + 0.5f - top) * static_cast<float>(stride);
        const float cx1 = (static_cast<float>(gx) + 0.5f + right) * static_cast<float>(stride);
        const float cy1 = (static_cast<float>(gy) + 0.5f + bottom) * static_cast<float>(stride);

        const float x0 = (cx0 - static_cast<float>(padX)) / scale;
        const float y0 = (cy0 - static_cast<float>(padY)) / scale;
        const float x1 = (cx1 - static_cast<float>(padX)) / scale;
        const float y1 = (cy1 - static_cast<float>(padY)) / scale;

        Proposal p;
        p.x0 = std::max(0.f, std::min(x0, static_cast<float>(imgW)));
        p.y0 = std::max(0.f, std::min(y0, static_cast<float>(imgH)));
        p.x1 = std::max(0.f, std::min(x1, static_cast<float>(imgW)));
        p.y1 = std::max(0.f, std::min(y1, static_cast<float>(imgH)));
        p.score = score;
        p.label = bestLabel;
        p.anchor = i;
        if (out1) {
            readMaskCoeffFromOut1(*out1, i, p.maskCoeff);
        }
        if (p.x1 > p.x0 + 4.f && p.y1 > p.y0 + 4.f) {
            props.push_back(p);
        }
    }
}

static void generateProposalsYolov8Transposed(
    const ncnn::Mat& feat,
    const ncnn::Mat* out1,
    float scale,
    int padX,
    int padY,
    int imgW,
    int imgH,
    bool excludePerson,
    std::vector<Proposal>& props) {
    const int numAnchors = feat.h;
    if (numAnchors <= 0 || feat.w < kDetFeatDim) return;

    for (int i = 0; i < numAnchors; ++i) {
        const float* row = feat.row(i);
        int gx = 0;
        int gy = 0;
        int stride = 8;
        gridCell(i, gx, gy, stride);

        const float left = decodeDfl(row + 0, kRegMax);
        const float top = decodeDfl(row + 16, kRegMax);
        const float right = decodeDfl(row + 32, kRegMax);
        const float bottom = decodeDfl(row + 48, kRegMax);

        int bestLabel = 0;
        float bestLogit = -1e9f;
        for (int c = 0; c < kNumClasses; ++c) {
            const float logit = row[64 + c];
            if (logit > bestLogit) {
                bestLogit = logit;
                bestLabel = c;
            }
        }
        const float score = sigmoid(bestLogit);
        if (score < scoreThreshold()) continue;
        if (excludePerson && bestLabel == kPersonClassId) continue;

        const float cx0 = (static_cast<float>(gx) + 0.5f - left) * static_cast<float>(stride);
        const float cy0 = (static_cast<float>(gy) + 0.5f - top) * static_cast<float>(stride);
        const float cx1 = (static_cast<float>(gx) + 0.5f + right) * static_cast<float>(stride);
        const float cy1 = (static_cast<float>(gy) + 0.5f + bottom) * static_cast<float>(stride);

        const float x0 = (cx0 - static_cast<float>(padX)) / scale;
        const float y0 = (cy0 - static_cast<float>(padY)) / scale;
        const float x1 = (cx1 - static_cast<float>(padX)) / scale;
        const float y1 = (cy1 - static_cast<float>(padY)) / scale;

        Proposal p;
        p.x0 = std::max(0.f, std::min(x0, static_cast<float>(imgW)));
        p.y0 = std::max(0.f, std::min(y0, static_cast<float>(imgH)));
        p.x1 = std::max(0.f, std::min(x1, static_cast<float>(imgW)));
        p.y1 = std::max(0.f, std::min(y1, static_cast<float>(imgH)));
        p.score = score;
        p.label = bestLabel;
        p.anchor = i;
        if (out1) {
            readMaskCoeffFromOut1(*out1, i, p.maskCoeff);
        }
        if (p.x1 > p.x0 + 4.f && p.y1 > p.y0 + 4.f) {
            props.push_back(p);
        }
    }
}

static void generateProposals(
    const ncnn::Mat& feat,
    const ncnn::Mat* out1,
    float scale,
    int padX,
    int padY,
    int imgW,
    int imgH,
    bool excludePerson,
    std::vector<Proposal>& props) {
    if (feat.h == kDetFeatDim || feat.h == 144) {
        generateProposalsYolov8(feat, out1, scale, padX, padY, imgW, imgH, excludePerson, props);
    } else if (feat.w == kDetFeatDim || feat.w == 144) {
        generateProposalsYolov8Transposed(feat, out1, scale, padX, padY, imgW, imgH, excludePerson, props);
    }
}

static Letterbox buildLetterbox(int width, int height) {
    Letterbox lb;
    lb.scale = std::min(
        static_cast<float>(kInputSize) / static_cast<float>(width),
        static_cast<float>(kInputSize) / static_cast<float>(height));
    lb.wPad = static_cast<int>(std::round(width * lb.scale));
    lb.hPad = static_cast<int>(std::round(height * lb.scale));
    lb.padX = (kInputSize - lb.wPad) / 2;
    lb.padY = (kInputSize - lb.hPad) / 2;
    return lb;
}

static ncnn::Mat letterboxInput(const unsigned char* rgb, int width, int height, const Letterbox& lb) {
    ncnn::Mat in =
        ncnn::Mat::from_pixels_resize(rgb, ncnn::Mat::PIXEL_RGB, width, height, lb.wPad, lb.hPad);
    ncnn::Mat padded;
    ncnn::copy_make_border(
        in,
        padded,
        lb.padY,
        kInputSize - lb.hPad - lb.padY,
        lb.padX,
        kInputSize - lb.wPad - lb.padX,
        ncnn::BORDER_CONSTANT,
        114.f);
    const float norm[3] = {1.f / 255.f, 1.f / 255.f, 1.f / 255.f};
    padded.substract_mean_normalize(nullptr, norm);
    return padded;
}

static float sampleBilinear(const std::vector<float>& src, int srcW, int srcH, float x, float y) {
    x = std::max(0.f, std::min(x, static_cast<float>(srcW - 1)));
    y = std::max(0.f, std::min(y, static_cast<float>(srcH - 1)));
    const int x0 = static_cast<int>(x);
    const int y0 = static_cast<int>(y);
    const int x1 = std::min(x0 + 1, srcW - 1);
    const int y1 = std::min(y0 + 1, srcH - 1);
    const float tx = x - static_cast<float>(x0);
    const float ty = y - static_cast<float>(y0);
    const float v00 = src[static_cast<size_t>(y0 * srcW + x0)];
    const float v10 = src[static_cast<size_t>(y0 * srcW + x1)];
    const float v01 = src[static_cast<size_t>(y1 * srcW + x0)];
    const float v11 = src[static_cast<size_t>(y1 * srcW + x1)];
    const float top = v00 + (v10 - v00) * tx;
    const float bot = v01 + (v11 - v01) * tx;
    return top + (bot - top) * ty;
}

static pretext::ContourPacket contourFromSegProposal(
    const Proposal& prop,
    const ncnn::Mat& proto,
    const Letterbox& lb,
    int imgW,
    int imgH) {
    pretext::ContourPacket empty;
    if (proto.empty() || proto.c < kMaskCoeffs) return empty;

    const int protoW = proto.w;
    const int protoH = proto.h;
    if (protoW <= 0 || protoH <= 0) return empty;

    const float bx0 = prop.x0 * lb.scale + static_cast<float>(lb.padX);
    const float by0 = prop.y0 * lb.scale + static_cast<float>(lb.padY);
    const float bx1 = prop.x1 * lb.scale + static_cast<float>(lb.padX);
    const float by1 = prop.y1 * lb.scale + static_cast<float>(lb.padY);

    const int px0 = std::max(0, static_cast<int>(std::floor(bx0 * 0.25f)));
    const int py0 = std::max(0, static_cast<int>(std::floor(by0 * 0.25f)));
    const int px1 = std::min(protoW, static_cast<int>(std::ceil(bx1 * 0.25f)));
    const int py1 = std::min(protoH, static_cast<int>(std::ceil(by1 * 0.25f)));
    const int cropProtoW = px1 - px0;
    const int cropProtoH = py1 - py0;
    if (cropProtoW < 2 || cropProtoH < 2) return empty;

    std::vector<float> protoCrop(static_cast<size_t>(cropProtoW * cropProtoH));
    for (int y = 0; y < cropProtoH; ++y) {
        for (int x = 0; x < cropProtoW; ++x) {
            float sum = 0.f;
            const int px = px0 + x;
            const int py = py0 + y;
            for (int c = 0; c < kMaskCoeffs; ++c) {
                const float* plane = proto.channel(c);
                sum += prop.maskCoeff[c] * plane[py * protoW + px];
            }
            protoCrop[static_cast<size_t>(y * cropProtoW + x)] = sigmoid(sum);
        }
    }

    const int outW = std::max(4, static_cast<int>(prop.x1 - prop.x0));
    const int outH = std::max(4, static_cast<int>(prop.y1 - prop.y0));
    std::vector<float> maskCrop(static_cast<size_t>(outW * outH));
    for (int y = 0; y < outH; ++y) {
        const float sy = (static_cast<float>(y) + 0.5f) / static_cast<float>(outH) * static_cast<float>(cropProtoH);
        for (int x = 0; x < outW; ++x) {
            const float sx = (static_cast<float>(x) + 0.5f) / static_cast<float>(outW) * static_cast<float>(cropProtoW);
            maskCrop[static_cast<size_t>(y * outW + x)] = sampleBilinear(protoCrop, cropProtoW, cropProtoH, sx, sy);
        }
    }

    return pretext::extractObjectContourFromMaskCrop(
        maskCrop.data(),
        outW,
        outH,
        prop.x0,
        prop.y0,
        imgW,
        imgH,
        false);
}

static bool runDetections(
    const unsigned char* rgb,
    int width,
    int height,
    bool excludePerson,
    std::vector<Proposal>& props,
    ncnn::Mat* protoOut) {
    if (!g_ready || !rgb || width <= 0 || height <= 0) return false;

    const Letterbox lb = buildLetterbox(width, height);
    ncnn::Mat padded = letterboxInput(rgb, width, height, lb);

    ncnn::Extractor ex = g_net.create_extractor();
    ex.input("in0", padded);
    ncnn::Mat feat;
    if (ex.extract("out0", feat) != 0) return false;

    ncnn::Mat out1;
    ncnn::Mat proto;
    const ncnn::Mat* out1Ptr = nullptr;
    if (g_kind == ModelKind::Seg) {
        if (ex.extract("out1", out1) != 0) return false;
        if (ex.extract("out2", proto) != 0) return false;
        out1Ptr = &out1;
        if (protoOut) *protoOut = proto;
    }

    generateProposals(feat, out1Ptr, lb.scale, lb.padX, lb.padY, width, height, excludePerson, props);

    if (props.empty() && excludePerson) {
        generateProposals(feat, out1Ptr, lb.scale, lb.padX, lb.padY, width, height, false, props);
        props.erase(
            std::remove_if(props.begin(), props.end(), [](const Proposal& p) {
                return p.label == kPersonClassId;
            }),
            props.end());
    }

    if (props.empty()) return false;
    nms(props, kNmsThr);
    return !props.empty();
}

static bool tryLoadModel(AAssetManager* mgr, const char* paramPath, const char* binPath, ModelKind kind) {
    g_net.clear();
    if (g_net.load_param(mgr, paramPath) != 0) return false;
    if (g_net.load_model(mgr, binPath) != 0) {
        g_net.clear();
        return false;
    }
    g_kind = kind;
    return true;
}

}

void setHighQuality(bool high) {
    pretext::NativeLock guard;
    g_highQuality = high;
    if (g_ready) {
        applyNetOptions();
    }
}

bool isSegmentationBackend() {
    pretext::NativeLock guard;
    return g_ready && g_kind == ModelKind::Seg;
}

bool isHighQuality() {
    pretext::NativeLock guard;
    return g_highQuality;
}

bool init(AAssetManager* mgr) {
    pretext::NativeLock guard;
    if (g_ready) return true;
    if (!mgr) return false;

    applyNetOptions();

    struct ModelCandidate {
        const char* param;
        const char* bin;
        ModelKind kind;
    };

    const ModelCandidate candidates[] = {
        {"vision/yolo11n_seg.param", "vision/yolo11n_seg.bin", ModelKind::Seg},
        {"vision/yolov8n_seg.param", "vision/yolov8n_seg.bin", ModelKind::Seg},
    };

    for (const ModelCandidate& candidate : candidates) {
        if (tryLoadModel(mgr, candidate.param, candidate.bin, candidate.kind)) {
            g_ready = true;
            return true;
        }
    }

    g_net.clear();
    g_kind = ModelKind::None;
    return false;
}

void release() {
    pretext::NativeLock guard;
    g_net.clear();
    g_ready = false;
    g_kind = ModelKind::None;
}

bool detectTopKWithContours(
    const unsigned char* rgb,
    int width,
    int height,
    DetectionWithContour* out,
    int maxK,
    int* outCount,
    bool excludePerson) {
    pretext::NativeLock guard;
    if (!g_ready || g_kind != ModelKind::Seg || !rgb || !out || !outCount ||
        width <= 0 || height <= 0 || maxK <= 0) {
        if (outCount) *outCount = 0;
        return false;
    }

    std::vector<Proposal> props;
    ncnn::Mat proto;
    if (!runDetections(rgb, width, height, excludePerson, props, &proto)) {
        *outCount = 0;
        return false;
    }

    const Letterbox lb = buildLetterbox(width, height);
    const int count = std::min(maxK, static_cast<int>(props.size()));
    for (int i = 0; i < count; ++i) {
        const Proposal& p = props[static_cast<size_t>(i)];
        DetectionWithContour& dc = out[i];
        dc.det.left = p.x0;
        dc.det.top = p.y0;
        dc.det.right = p.x1;
        dc.det.bottom = p.y1;
        dc.det.score = p.score;
        dc.det.classId = p.label;
        dc.contour = contourFromSegProposal(p, proto, lb, width, height);
    }
    *outCount = count;
    return count > 0;
}

const char* classLabel(int classId) {
    if (classId < 0 || classId >= 80) return "object";
    return kCoco[classId];
}

}
