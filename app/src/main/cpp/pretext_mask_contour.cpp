/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_contour_internal.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <numeric>
#include <vector>

namespace pretext {
namespace contour {
namespace {

static float maskAt(const float* mask, int w, int h, int x, int y) {
    if (x < 0 || y < 0 || x >= w || y >= h) return 0.f;
    return mask[static_cast<size_t>(y * w + x)];
}

static std::vector<uint8_t> binarizeMask(const float* mask, int w, int h, float iso) {
    const int total = w * h;
    std::vector<uint8_t> bin(static_cast<size_t>(total));
    for (int i = 0; i < total; ++i) {
        bin[static_cast<size_t>(i)] = mask[i] >= iso ? 1 : 0;
    }
    return bin;
}


static std::vector<uint8_t> hysteresisBinarize(
    const float* mask,
    int w,
    int h,
    float highIso,
    float lowIso) {
    const int total = w * h;
    const float lo = std::min(highIso, lowIso);
    const float hi = std::max(highIso, lowIso);
    std::vector<uint8_t> strong(static_cast<size_t>(total), 0);
    std::vector<uint8_t> weak(static_cast<size_t>(total), 0);
    for (int i = 0; i < total; ++i) {
        const float v = mask[i];
        if (v >= hi) {
            strong[static_cast<size_t>(i)] = 1;
            weak[static_cast<size_t>(i)] = 1;
        } else if (v >= lo) {
            weak[static_cast<size_t>(i)] = 1;
        }
    }
    std::vector<uint8_t> out(static_cast<size_t>(total), 0);
    std::vector<std::pair<int, int>> stack;
    stack.reserve(256);
    for (int y = 0; y < h; ++y) {
        for (int x = 0; x < w; ++x) {
            const size_t idx = static_cast<size_t>(y * w + x);
            if (!strong[idx] || out[idx]) continue;
            stack.emplace_back(x, y);
            out[idx] = 1;
            while (!stack.empty()) {
                const auto [cx, cy] = stack.back();
                stack.pop_back();
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dx = -1; dx <= 1; ++dx) {
                        const int nx = cx + dx;
                        const int ny = cy + dy;
                        if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                        const size_t nidx = static_cast<size_t>(ny * w + nx);
                        if (!weak[nidx] || out[nidx]) continue;
                        out[nidx] = 1;
                        stack.emplace_back(nx, ny);
                    }
                }
            }
        }
    }
    return out;
}

static Vec2 interpolateIsoEdge(float v0, float v1, const Vec2& p0, const Vec2& p1, float iso) {
    const float denom = v1 - v0;
    const float t = std::fabs(denom) > 1e-5f ? clampf((iso - v0) / denom, 0.f, 1.f) : 0.5f;
    return lerp(p0, p1, t);
}


static void appendMarchingSquaresCell(
    const float* mask,
    int w,
    int h,
    int cx,
    int cy,
    float iso,
    std::vector<Vec2>& out) {
    const float v00 = maskAt(mask, w, h, cx, cy);
    const float v10 = maskAt(mask, w, h, cx + 1, cy);
    const float v11 = maskAt(mask, w, h, cx + 1, cy + 1);
    const float v01 = maskAt(mask, w, h, cx, cy + 1);
    const int b00 = v00 >= iso ? 1 : 0;
    const int b10 = v10 >= iso ? 1 : 0;
    const int b11 = v11 >= iso ? 1 : 0;
    const int b01 = v01 >= iso ? 1 : 0;
    const int code = b00 | (b10 << 1) | (b11 << 2) | (b01 << 3);
    if (code == 0 || code == 15) return;

    const Vec2 p00{static_cast<float>(cx) + 0.5f, static_cast<float>(cy) + 0.5f};
    const Vec2 p10{static_cast<float>(cx + 1) + 0.5f, static_cast<float>(cy) + 0.5f};
    const Vec2 p11{static_cast<float>(cx + 1) + 0.5f, static_cast<float>(cy + 1) + 0.5f};
    const Vec2 p01{static_cast<float>(cx) + 0.5f, static_cast<float>(cy + 1) + 0.5f};

    auto pushEdge = [&](const Vec2& a, const Vec2& b, float va, float vb) {
        out.push_back(interpolateIsoEdge(va, vb, a, b, iso));
    };

    switch (code) {
        case 1:
        case 14:
            pushEdge(p00, p10, v00, v10);
            pushEdge(p01, p00, v01, v00);
            break;
        case 2:
        case 13:
            pushEdge(p10, p11, v10, v11);
            pushEdge(p00, p10, v00, v10);
            break;
        case 3:
        case 12:
            pushEdge(p01, p00, v01, v00);
            pushEdge(p10, p11, v10, v11);
            break;
        case 4:
        case 11:
            pushEdge(p11, p01, v11, v01);
            pushEdge(p10, p11, v10, v11);
            break;
        case 5:
            pushEdge(p00, p10, v00, v10);
            pushEdge(p11, p01, v11, v01);
            break;
        case 6:
        case 9:
            pushEdge(p00, p10, v00, v10);
            pushEdge(p11, p01, v11, v01);
            break;
        case 7:
        case 8:
            pushEdge(p01, p00, v01, v00);
            pushEdge(p11, p01, v11, v01);
            break;
        case 10:
            pushEdge(p10, p11, v10, v11);
            pushEdge(p01, p00, v01, v00);
            break;
        default:
            break;
    }
}

static float scoreContourRing(const std::vector<Vec2>& ring, int w, int h) {
    if (ring.size() < 6) return -1.f;
    const float total = static_cast<float>(w * h);
    const float areaPx = shoelaceAreaAbs(ring);
    const float areaNorm = areaPx / total;
    if (areaNorm < 0.003f || areaNorm > 0.88f) return -1.f;

    float cx = 0.f;
    float cy = 0.f;
    for (const Vec2& p : ring) {
        cx += p.x;
        cy += p.y;
    }
    cx /= static_cast<float>(ring.size());
    cy /= static_cast<float>(ring.size());
    const float dx = cx - static_cast<float>(w) * 0.5f;
    const float dy = cy - static_cast<float>(h) * 0.5f;
    const float centerDist =
        std::sqrt(dx * dx + dy * dy) /
        (std::sqrt(static_cast<float>(w * w + h * h)) * 0.5f + 1e-3f);

    const float solidity = contourSolidity(ring);
    if (solidity < 0.32f) return -1.f;

    float score = solidity;
    if (areaNorm >= 0.02f && areaNorm <= 0.58f) {
        score *= 1.25f;
    } else if (areaNorm > 0.72f) {
        score *= 0.22f;
    }
    score *= (1.f - 0.42f * centerDist);
    score *= std::sqrt(clampf(areaNorm, 0.004f, 1.f));
    return score;
}

static bool isForeground(const std::vector<uint8_t>& bin, int w, int h, int x, int y) {
    return x >= 0 && y >= 0 && x < w && y < h && bin[static_cast<size_t>(y * w + x)] != 0;
}

static bool isBoundaryPixel(const std::vector<uint8_t>& bin, int w, int h, int x, int y) {
    if (!isForeground(bin, w, h, x, y)) return false;
    for (int dy = -1; dy <= 1; ++dy) {
        for (int dx = -1; dx <= 1; ++dx) {
            if (dx == 0 && dy == 0) continue;
            if (!isForeground(bin, w, h, x + dx, y + dy)) return true;
        }
    }
    return false;
}

static void morphErode3x3(const std::vector<uint8_t>& src, std::vector<uint8_t>& dst, int w, int h) {
    dst.assign(src.size(), 0);
    for (int y = 0; y < h; ++y) {
        for (int x = 0; x < w; ++x) {
            bool allOn = true;
            for (int dy = -1; dy <= 1 && allOn; ++dy) {
                for (int dx = -1; dx <= 1; ++dx) {
                    if (!isForeground(src, w, h, x + dx, y + dy)) {
                        allOn = false;
                        break;
                    }
                }
            }
            if (allOn) dst[static_cast<size_t>(y * w + x)] = 1;
        }
    }
}

static void morphDilate3x3(const std::vector<uint8_t>& src, std::vector<uint8_t>& dst, int w, int h) {
    dst.assign(src.size(), 0);
    for (int y = 0; y < h; ++y) {
        for (int x = 0; x < w; ++x) {
            bool anyOn = false;
            for (int dy = -1; dy <= 1 && !anyOn; ++dy) {
                for (int dx = -1; dx <= 1; ++dx) {
                    if (isForeground(src, w, h, x + dx, y + dy)) {
                        anyOn = true;
                        break;
                    }
                }
            }
            if (anyOn) dst[static_cast<size_t>(y * w + x)] = 1;
        }
    }
}

static void morphOpen3x3(std::vector<uint8_t>& bin, int w, int h) {
    std::vector<uint8_t> tmp;
    morphErode3x3(bin, tmp, w, h);
    morphDilate3x3(tmp, bin, w, h);
}

static void morphClose3x3(std::vector<uint8_t>& bin, int w, int h) {
    std::vector<uint8_t> tmp;
    morphDilate3x3(bin, tmp, w, h);
    morphErode3x3(tmp, bin, w, h);
}

struct BlobStats {
    int area = 0;
    int minX = 0;
    int minY = 0;
    int maxX = 0;
    int maxY = 0;
    long long sumX = 0;
    long long sumY = 0;
};

static int borderTouchCount(const BlobStats& b, int w, int h) {
    int touches = 0;
    if (b.minX <= 1) ++touches;
    if (b.minY <= 1) ++touches;
    if (b.maxX >= w - 2) ++touches;
    if (b.maxY >= h - 2) ++touches;
    return touches;
}

static float scorePersonBlob(const BlobStats& b, int w, int h) {
    if (b.area < 64) return -1.f;
    const float total = static_cast<float>(w * h);
    const float areaNorm = static_cast<float>(b.area) / total;
    const int borders = borderTouchCount(b, w, h);

    if (areaNorm > 0.84f && borders >= 3) return -1.f;

    const float cx = static_cast<float>(b.sumX) / static_cast<float>(b.area);
    const float cy = static_cast<float>(b.sumY) / static_cast<float>(b.area);
    const float dx = cx - static_cast<float>(w) * 0.5f;
    const float dy = cy - static_cast<float>(h) * 0.5f;
    const float maxDist = std::sqrt(
        static_cast<float>(w * w + h * h)) *
        0.5f;
    const float centerDist = std::sqrt(dx * dx + dy * dy) / (maxDist + 1e-3f);

    float score = areaNorm;
    score *= (1.f - 0.11f * static_cast<float>(borders));
    score *= (1.f - 0.38f * centerDist);

    if (areaNorm < 0.012f) score *= 0.45f;
    if (areaNorm > 0.72f) score *= 0.28f;
    else if (areaNorm > 0.55f) score *= 0.55f;
    else if (areaNorm >= 0.02f && areaNorm <= 0.50f) score *= 1.12f;

    return score;
}


static std::vector<Vec2> mooreTraceOuterBoundary(const std::vector<uint8_t>& bin, int w, int h) {
    int sx = -1;
    int sy = -1;
    for (int y = 0; y < h && sx < 0; ++y) {
        for (int x = 0; x < w; ++x) {
            if (isBoundaryPixel(bin, w, h, x, y)) {
                sx = x;
                sy = y;
                break;
            }
        }
    }
    if (sx < 0) return {};

    static constexpr int kNx[8] = {1, 1, 0, -1, -1, -1, 0, 1};
    static constexpr int kNy[8] = {0, 1, 1, 1, 0, -1, -1, -1};

    std::vector<Vec2> path;
    path.reserve(512);
    int cx = sx;
    int cy = sy;
    int dir = 0;
    path.push_back({static_cast<float>(cx) + 0.5f, static_cast<float>(cy) + 0.5f});

    const int guardMax = w * h * 4;
    for (int guard = 0; guard < guardMax; ++guard) {
        const int startDir = (dir + 7) % 8;
        bool stepped = false;
        for (int i = 0; i < 8; ++i) {
            const int nd = (startDir + i) % 8;
            const int nx = cx + kNx[nd];
            const int ny = cy + kNy[nd];
            if (!isBoundaryPixel(bin, w, h, nx, ny)) continue;
            cx = nx;
            cy = ny;
            dir = nd;
            path.push_back({static_cast<float>(cx) + 0.5f, static_cast<float>(cy) + 0.5f});
            stepped = true;
            break;
        }
        if (!stepped) break;
        if (cx == sx && cy == sy && path.size() >= 6) break;
    }

    if (path.size() > 2) {
        const Vec2& f = path.front();
        const Vec2& l = path.back();
        if (len(f - l) < 0.5f) path.pop_back();
    }
    if (path.size() < 6) return {};
    constexpr size_t kMaxTracePoints = 320;
    if (path.size() > kMaxTracePoints) {
        std::vector<Vec2> reduced;
        reduced.reserve(kMaxTracePoints);
        const size_t step = path.size() / kMaxTracePoints;
        const size_t stride = step < 1 ? 1 : step;
        for (size_t i = 0; i < path.size(); i += stride) {
            reduced.push_back(path[i]);
        }
        return reduced.size() >= 6 ? reduced : path;
    }
    return path;
}

static std::vector<Vec2> contourAtIso(const float* mask, int w, int h, float iso, bool useHysteresis) {
    std::vector<uint8_t> bin;
    if (useHysteresis) {
        bin = hysteresisBinarize(mask, w, h, iso, iso - 0.075f);
    } else {
        bin = binarizeMask(mask, w, h, iso);
    }
    const int fgCount = static_cast<int>(std::accumulate(bin.begin(), bin.end(), 0));
    if (fgCount < 64) return {};

    morphOpen3x3(bin, w, h);
    morphClose3x3(bin, w, h);
    keepBestForegroundBlob(bin, w, h);

    const int fgAfter = static_cast<int>(std::accumulate(bin.begin(), bin.end(), 0));
    if (fgAfter < 64) return {};

    std::vector<Vec2> trace = mooreTraceOuterBoundary(bin, w, h);
    if (trace.size() >= 6) return trace;

    std::vector<Vec2> ms;
    ms.reserve(256);
    for (int y = 0; y < h - 1; ++y) {
        for (int x = 0; x < w - 1; ++x) {
            appendMarchingSquaresCell(mask, w, h, x, y, iso, ms);
        }
    }
    if (ms.size() < 6) return {};
    const float diag = std::sqrt(static_cast<float>(w * w + h * h));
    ms = simplifyClosedRdp(ms, diag * 0.004f, 160);
    return ms.size() >= 6 ? ms : trace;
}

static std::vector<Vec2> contourAtIso(const float* mask, int w, int h, float iso) {
    return contourAtIso(mask, w, h, iso, true);
}

static float foregroundPercentile(const float* mask, int total, float quantile) {
    std::vector<float> vals;
    vals.reserve(static_cast<size_t>(total / 4));
    for (int i = 0; i < total; ++i) {
        if (mask[i] >= kMaskForegroundMin) vals.push_back(mask[i]);
    }
    if (vals.size() < 64) return kDefaultMaskThreshold;
    const size_t idx = static_cast<size_t>(
        clampf(quantile, 0.f, 1.f) * static_cast<float>(vals.size() - 1));
    std::nth_element(vals.begin(), vals.begin() + static_cast<std::ptrdiff_t>(idx), vals.end());
    return clampf(vals[idx] * 0.92f, 0.30f, 0.58f);
}

}

std::vector<float> downsampleMaskNearest(
    const float* mask,
    int inW,
    int inH,
    int outW,
    int outH) {
    if (!mask || inW <= 0 || inH <= 0 || outW <= 0 || outH <= 0) return {};
    std::vector<float> out(static_cast<size_t>(outW * outH));
    for (int y = 0; y < outH; ++y) {
        const int sy = std::min(inH - 1, (y * inH) / outH);
        for (int x = 0; x < outW; ++x) {
            const int sx = std::min(inW - 1, (x * inW) / outW);
            out[static_cast<size_t>(y * outW + x)] = mask[sy * inW + sx];
        }
    }
    return out;
}

float computeOtsuIsoLevel(const float* mask, int total) {
    int hist[64] = {};
    int fgPixels = 0;
    for (int i = 0; i < total; ++i) {
        if (mask[i] < kMaskForegroundMin) continue;
        const int bin = static_cast<int>(clampf(mask[i], 0.f, 0.999f) * 63.f);
        hist[bin]++;
        fgPixels++;
    }
    if (fgPixels < 64) return kDefaultMaskThreshold;

    float sum = 0.f;
    for (int i = 0; i < 64; ++i) {
        sum += static_cast<float>(hist[i]) * (static_cast<float>(i) / 63.f);
    }
    float sumB = 0.f;
    int wB = 0;
    float maxVar = -1.f;
    float bestT = kDefaultMaskThreshold;
    for (int t = 0; t < 64; ++t) {
        wB += hist[t];
        if (wB == 0) continue;
        const int wF = fgPixels - wB;
        if (wF == 0) break;
        sumB += static_cast<float>(hist[t]) * (static_cast<float>(t) / 63.f);
        const float mB = sumB / static_cast<float>(wB);
        const float mF = (sum - sumB) / static_cast<float>(wF);
        const float between = static_cast<float>(wB) * static_cast<float>(wF) * (mB - mF) * (mB - mF);
        if (between > maxVar) {
            maxVar = between;
            bestT = static_cast<float>(t) / 63.f;
        }
    }
    return clampf(bestT, 0.30f, 0.58f);
}

float computeAdaptiveIsoLevel(const float* mask, int total, int w, int h) {
    (void)w;
    (void)h;
    const float otsu = computeOtsuIsoLevel(mask, total);
    const float p62 = foregroundPercentile(mask, total, 0.62f);
    return clampf(otsu * 0.52f + p62 * 0.48f, 0.30f, 0.56f);
}

void keepLargestForegroundBlob(std::vector<uint8_t>& bin, int w, int h) {
    keepBestForegroundBlob(bin, w, h);
}

void keepBestForegroundBlob(std::vector<uint8_t>& bin, int w, int h) {
    const int total = w * h;
    std::vector<int> labels(static_cast<size_t>(total), 0);
    std::vector<BlobStats> blobs;
    blobs.reserve(8);
    int nextLabel = 1;

    auto flood = [&](int sx, int sy, int label) {
        BlobStats stats;
        stats.minX = sx;
        stats.minY = sy;
        stats.maxX = sx;
        stats.maxY = sy;
        std::vector<std::pair<int, int>> stack;
        stack.emplace_back(sx, sy);
        while (!stack.empty()) {
            const auto [x, y] = stack.back();
            stack.pop_back();
            const size_t idx = static_cast<size_t>(y * w + x);
            if (x < 0 || y < 0 || x >= w || y >= h || !bin[idx] || labels[idx] != 0) continue;
            labels[idx] = label;
            ++stats.area;
            stats.sumX += x;
            stats.sumY += y;
            stats.minX = std::min(stats.minX, x);
            stats.minY = std::min(stats.minY, y);
            stats.maxX = std::max(stats.maxX, x);
            stats.maxY = std::max(stats.maxY, y);
            stack.emplace_back(x + 1, y);
            stack.emplace_back(x - 1, y);
            stack.emplace_back(x, y + 1);
            stack.emplace_back(x, y - 1);
        }
        return stats;
    };

    for (int y = 0; y < h; ++y) {
        for (int x = 0; x < w; ++x) {
            const size_t idx = static_cast<size_t>(y * w + x);
            if (!bin[idx] || labels[idx] != 0) continue;
            blobs.push_back(flood(x, y, nextLabel++));
        }
    }
    if (blobs.empty()) return;

    int bestLabel = 0;
    float bestScore = -1.f;
    for (size_t i = 0; i < blobs.size(); ++i) {
        const float score = scorePersonBlob(blobs[i], w, h);
        if (score > bestScore) {
            bestScore = score;
            bestLabel = static_cast<int>(i) + 1;
        }
    }
    if (bestLabel == 0) return;

    for (int i = 0; i < total; ++i) {
        if (bin[static_cast<size_t>(i)] && labels[static_cast<size_t>(i)] != bestLabel) {
            bin[static_cast<size_t>(i)] = 0;
        }
    }
}

void boxBlurMask3x3(std::vector<float>& mask, int w, int h) {
    const int n = w * h;
    if (n < 9) return;
    std::vector<float> tmp(static_cast<size_t>(n));
    for (int y = 0; y < h; ++y) {
        for (int x = 0; x < w; ++x) {
            float sum = 0.f;
            int count = 0;
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dx = -1; dx <= 1; ++dx) {
                    sum += maskAt(mask.data(), w, h, x + dx, y + dy);
                    ++count;
                }
            }
            tmp[static_cast<size_t>(y * w + x)] = sum / static_cast<float>(count);
        }
    }
    mask.swap(tmp);
}

std::vector<Vec2> extractBestPersonIsoContour(const float* mask, int w, int h) {
    if (!mask || w < 8 || h < 8) return {};

    const int total = w * h;
    const float adaptive = computeAdaptiveIsoLevel(mask, total, w, h);
    const float otsu = computeOtsuIsoLevel(mask, total);
    const float p50 = foregroundPercentile(mask, total, 0.50f);
    const float p68 = foregroundPercentile(mask, total, 0.68f);

    const float isoLevels[] = {
        adaptive,
        otsu,
        clampf(adaptive * 0.55f + otsu * 0.45f, 0.30f, 0.56f),
        clampf(adaptive + 0.06f, 0.30f, 0.58f),
        clampf(adaptive + 0.11f, 0.32f, 0.58f),
        clampf(adaptive - 0.05f, 0.28f, 0.56f),
        p50,
        p68,
    };

    float bestScore = -1.f;
    std::vector<Vec2> best;
    for (float iso : isoLevels) {
        std::vector<Vec2> ring = contourAtIso(mask, w, h, iso, true);
        const float score = scoreContourRing(ring, w, h);
        if (score > bestScore) {
            bestScore = score;
            best = std::move(ring);
        }
    }
    return best;
}

std::vector<Vec2> marchingSquaresIsoContour(const float* mask, int w, int h, float iso) {
    if (!mask || w < 3 || h < 3) return {};

    std::vector<Vec2> contour = contourAtIso(mask, w, h, iso, true);
    if (contour.size() >= 6) return contour;

    constexpr float kFallbacks[] = {0.34f, 0.38f, 0.42f, 0.46f, 0.50f, 0.54f};
    for (float t : kFallbacks) {
        if (std::fabs(t - iso) < 0.015f) continue;
        contour = contourAtIso(mask, w, h, t, true);
        if (contour.size() >= 6) return contour;
    }
    return extractBestPersonIsoContour(mask, w, h);
}

float normBoundsArea(const Bounds& b) {
    const float w = std::max(0.f, b.right - b.left);
    const float h = std::max(0.f, b.bottom - b.top);
    return w * h;
}

}
}
