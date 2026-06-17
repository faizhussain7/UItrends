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
constexpr int kFeatDim = 64 + kNumClasses;
constexpr float kScoreThr = 0.30f;
constexpr float kNmsThr = 0.45f;
constexpr int kPersonClassId = 0;

ncnn::Net g_net;
bool g_ready = false;

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
    float x0, y0, x1, y1, score;
    int label;
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
            if (p.label == k.label && iou(p, k) > thr) {
                drop = true;
                break;
            }
        }
        if (!drop) kept.push_back(p);
    }
    props.swap(kept);
}


static void generateProposalsYolov8(
    const ncnn::Mat& feat,
    float scale,
    int padX,
    int padY,
    int imgW,
    int imgH,
    bool excludePerson,
    std::vector<Proposal>& props) {
    const int numAnchors = feat.w;
    const int channels = feat.h;
    if (numAnchors <= 0 || channels < kFeatDim) return;

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
        if (score < kScoreThr) continue;
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
        if (p.x1 > p.x0 + 4.f && p.y1 > p.y0 + 4.f) {
            props.push_back(p);
        }
    }
}


static void generateProposalsYolov8Transposed(
    const ncnn::Mat& feat,
    float scale,
    int padX,
    int padY,
    int imgW,
    int imgH,
    bool excludePerson,
    std::vector<Proposal>& props) {
    const int numAnchors = feat.h;
    if (numAnchors <= 0 || feat.w < kFeatDim) return;

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
        if (score < kScoreThr) continue;
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
        if (p.x1 > p.x0 + 4.f && p.y1 > p.y0 + 4.f) {
            props.push_back(p);
        }
    }
}

static void generateProposals(
    const ncnn::Mat& feat,
    float scale,
    int padX,
    int padY,
    int imgW,
    int imgH,
    bool excludePerson,
    std::vector<Proposal>& props) {
    if (feat.h == kFeatDim || feat.h == 144) {
        generateProposalsYolov8(feat, scale, padX, padY, imgW, imgH, excludePerson, props);
    } else if (feat.w == kFeatDim || feat.w == 144) {
        generateProposalsYolov8Transposed(feat, scale, padX, padY, imgW, imgH, excludePerson, props);
    }
}

}

bool init(AAssetManager* mgr) {
    pretext::NativeLock guard;
    if (g_ready) return true;
    if (!mgr) return false;
    ncnn::Option opt;
    opt.num_threads = 1;
    opt.use_vulkan_compute = false;
    g_net.opt = opt;
    if (g_net.load_param(mgr, "vision/yolov8n.param") != 0) {
        return false;
    }
    if (g_net.load_model(mgr, "vision/yolov8n.bin") != 0) {
        return false;
    }
    g_ready = true;
    return true;
}

void release() {
    pretext::NativeLock guard;
    g_net.clear();
    g_ready = false;
}

bool detectBest(
    const unsigned char* rgb,
    int width,
    int height,
    Detection* out,
    bool excludePerson) {
    pretext::NativeLock guard;
    if (!g_ready || !rgb || !out || width <= 0 || height <= 0) return false;

    const float scale = std::min(
        static_cast<float>(kInputSize) / static_cast<float>(width),
        static_cast<float>(kInputSize) / static_cast<float>(height));
    const int wPad = static_cast<int>(std::round(width * scale));
    const int hPad = static_cast<int>(std::round(height * scale));
    const int padX = (kInputSize - wPad) / 2;
    const int padY = (kInputSize - hPad) / 2;

    ncnn::Mat in =
        ncnn::Mat::from_pixels_resize(rgb, ncnn::Mat::PIXEL_RGB, width, height, wPad, hPad);
    ncnn::Mat padded;
    ncnn::copy_make_border(
        in, padded, padY, kInputSize - hPad - padY, padX, kInputSize - wPad - padX,
        ncnn::BORDER_CONSTANT, 114.f);

    const float norm[3] = {1.f / 255.f, 1.f / 255.f, 1.f / 255.f};
    padded.substract_mean_normalize(nullptr, norm);

    ncnn::Extractor ex = g_net.create_extractor();
    ex.input("in0", padded);
    ncnn::Mat feat;
    if (ex.extract("out0", feat) != 0) return false;

    std::vector<Proposal> props;
    generateProposals(feat, scale, padX, padY, width, height, excludePerson, props);

    if (props.empty() && excludePerson) {
        generateProposals(feat, scale, padX, padY, width, height, false, props);
        props.erase(
            std::remove_if(props.begin(), props.end(), [](const Proposal& p) {
                return p.label == kPersonClassId;
            }),
            props.end());
    }

    if (props.empty()) return false;
    nms(props, kNmsThr);

    const Proposal& best = props[0];
    out->left = best.x0;
    out->top = best.y0;
    out->right = best.x1;
    out->bottom = best.y1;
    out->score = best.score;
    out->classId = best.label;
    return true;
}

const char* classLabel(int classId) {
    if (classId < 0 || classId >= 80) return "object";
    return kCoco[classId];
}

}
