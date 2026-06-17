/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_geometry.h"

#include "pretext_contour_internal.h"
#include "pretext_native_lock.h"

#include <algorithm>
#include <cmath>
#include <vector>
#include <numeric>

namespace pretext {
namespace {

using namespace contour;

struct Lm {
    float x = 0.f;
    float y = 0.f;
    float l = 0.f;
    bool ok = false;
};

static Lm readLm(const float* data, int idx) {
    const float* p = data + idx * 3;
    return Lm{p[0], p[1], p[2], p[2] >= kMinLikelihood};
}

static std::vector<Vec2> g_prevNorm[kSmoothChannels];
static Bounds g_prevBounds[kSmoothChannels];
static bool g_hasPrev[kSmoothChannels] = {false, false, false};
static ContourPacket g_lastGoodPkt[kSmoothChannels];
static bool g_hasLastGood[kSmoothChannels] = {false, false, false};

static float boundsCenterX(const Bounds& b) {
    return (b.left + b.right) * 0.5f;
}

static float boundsCenterY(const Bounds& b) {
    return (b.top + b.bottom) * 0.5f;
}

static void push(std::vector<Vec2>& out, const Vec2& p) {
    if (out.empty()) {
        out.push_back(p);
        return;
    }
    const Vec2& last = out.back();
    const float dx = p.x - last.x;
    const float dy = p.y - last.y;
    if (dx * dx + dy * dy > 1.44f) out.push_back(p);
}

static void pushArc(
    std::vector<Vec2>& out,
    Vec2 center,
    float radius,
    float a0,
    float a1,
    int segments) {
    for (int i = 0; i <= segments; ++i) {
        const float t = static_cast<float>(i) / static_cast<float>(segments);
        const float a = a0 + (a1 - a0) * t;
        push(out, {center.x + radius * std::cos(a), center.y + radius * std::sin(a)});
    }
}

static Vec2 pushOut(const Vec2& p, const Vec2& center, float amount) {
    return p + norm(p - center) * amount;
}

static Vec2 limbPoint(const Vec2& a, const Vec2& b, const Vec2& joint, float halfWidth, bool rightSide) {
    const Vec2 tangent = norm(b - a);
    Vec2 n{-tangent.y, tangent.x};
    if ((rightSide && n.x < 0.f) || (!rightSide && n.x > 0.f)) n = n * -1.f;
    return joint + n * halfWidth;
}

static ContourPacket refineWithPoseExtremities(
    const ContourPacket& maskContour,
    const float* landmarks,
    int landmarkCount,
    int imageW,
    int imageH) {
    if (maskContour.norm.size() < 6 || !landmarks || landmarkCount < 10) return maskContour;

    std::vector<Vec2> ring = normToPixels(maskContour, imageW, imageH);
    bool changed = false;

    auto tryExtremity = [&](int idx, float minLikelihood, float minOutsidePx) {
        const Lm lm = readLm(landmarks, idx);
        if (!lm.ok || lm.l < minLikelihood) return;
        const Vec2 p{lm.x, lm.y};
        if (pointInsidePolygon(ring, p)) return;
        float minD = 1e30f;
        for (size_t i = 0; i < ring.size(); ++i) {
            minD = std::min(minD, distPointToSegment(p, ring[i], ring[(i + 1) % ring.size()]));
        }
        if (minD < minOutsidePx) return;
        insertVertexOnRing(ring, p);
        changed = true;
    };

    tryExtremity(7, 0.40f, 4.f);
    tryExtremity(8, 0.40f, 4.f);
    tryExtremity(15, 0.36f, 6.f);
    tryExtremity(16, 0.36f, 6.f);
    tryExtremity(13, 0.34f, 5.f);
    tryExtremity(14, 0.34f, 5.f);
    tryExtremity(27, 0.34f, 5.f);
    tryExtremity(28, 0.34f, 5.f);

    if (!changed) return maskContour;

    const float diag = std::sqrt(
        static_cast<float>(imageW) * static_cast<float>(imageW) +
        static_cast<float>(imageH) * static_cast<float>(imageH));
    ring = simplifyClosedRdp(ring, diag * 0.0025f, kMaxContourVerts);
    smoothChaikinClosed(ring, 1);
    return packPolygon(ring, imageW, imageH);
}

static float polygonNormAreaPx(const std::vector<Vec2>& px, int imageW, int imageH) {
    const float denom = static_cast<float>(imageW * imageH);
    if (denom < 1.f) return 0.f;
    return contour::shoelaceAreaAbs(px) / denom;
}

static std::vector<Vec2> scaleMaskContourToImage(
    const std::vector<Vec2>& maskSpace,
    int maskW,
    int maskH,
    int imageW,
    int imageH) {
    const float scaleX = static_cast<float>(imageW) / static_cast<float>(maskW);
    const float scaleY = static_cast<float>(imageH) / static_cast<float>(maskH);
    std::vector<Vec2> scaled;
    scaled.reserve(maskSpace.size());
    for (const Vec2& p : maskSpace) {
        scaled.push_back({p.x * scaleX, p.y * scaleY});
    }
    return scaled;
}

}

ContourPacket contourFromSegmentationMask(
    const float* mask,
    int maskW,
    int maskH,
    int imageW,
    int imageH) {
    ContourPacket empty;
    if (!mask || maskW < 8 || maskH < 8 || imageW <= 0 || imageH <= 0) return empty;

    constexpr int kMaxMaskSide = 256;
    int dsW = maskW;
    int dsH = maskH;
    if (dsW > kMaxMaskSide || dsH > kMaxMaskSide) {
        if (dsW >= dsH) {
            dsW = kMaxMaskSide;
            dsH = std::max(8, static_cast<int>(std::lround(static_cast<float>(maskH) * (static_cast<float>(dsW) / static_cast<float>(maskW)))));
        } else {
            dsH = kMaxMaskSide;
            dsW = std::max(8, static_cast<int>(std::lround(static_cast<float>(maskW) * (static_cast<float>(dsH) / static_cast<float>(maskH)))));
        }
    }

    std::vector<float> smoothed;
    if (dsW != maskW || dsH != maskH) {
        smoothed = downsampleMaskNearest(mask, maskW, maskH, dsW, dsH);
    } else {
        smoothed.assign(mask, mask + static_cast<size_t>(maskW * maskH));
    }
    if (smoothed.size() < static_cast<size_t>(dsW * dsH)) return empty;
    boxBlurMask3x3(smoothed, dsW, dsH);

    std::vector<Vec2> contourMask = extractBestPersonIsoContour(smoothed.data(), dsW, dsH);
    if (contourMask.size() < 6) return empty;

    std::vector<Vec2> scaled = scaleMaskContourToImage(contourMask, dsW, dsH, imageW, imageH);
    const float diag = std::sqrt(
        static_cast<float>(imageW) * static_cast<float>(imageW) +
        static_cast<float>(imageH) * static_cast<float>(imageH));
    scaled = simplifyClosedRdp(scaled, diag * 0.0025f, kMaxContourVerts);
    smoothChaikinClosed(scaled, 1);
    ContourPacket pkt = packPolygon(scaled, imageW, imageH);

    float area = polygonNormAreaPx(scaled, imageW, imageH);
    if (area > kMaxPersonNormArea + 0.12f) {
        return empty;
    }
    if (area < 0.004f) {
        return empty;
    }
    return pkt;
}

ContourPacket extractPersonContour(
    const float* mask,
    int maskW,
    int maskH,
    const float* landmarks,
    int landmarkCount,
    int imageW,
    int imageH) {
    ContourPacket pkt = contourFromSegmentationMask(mask, maskW, maskH, imageW, imageH);
    if (pkt.norm.size() >= 6 && landmarks && landmarkCount >= 10) {
        pkt = refineWithPoseExtremities(pkt, landmarks, landmarkCount, imageW, imageH);
    }
    return smoothTemporal(pkt, 0.44f, 0);
}

ContourPacket extractFaceContour(
    const float* polylineXy,
    int pointCount,
    float cx,
    float cy,
    float rx,
    float ry,
    int imageW,
    int imageH) {
    if (polylineXy && pointCount >= 6) {
        std::vector<Vec2> ring;
        ring.reserve(static_cast<size_t>(pointCount));
        for (int i = 0; i < pointCount; ++i) {
            ring.push_back({polylineXy[i * 2], polylineXy[i * 2 + 1]});
        }
        const float diag = std::sqrt(
            static_cast<float>(imageW) * static_cast<float>(imageW) +
            static_cast<float>(imageH) * static_cast<float>(imageH));
        ring = simplifyClosedRdp(ring, diag * 0.002f, 80);
        smoothChaikinClosed(ring, 1);
        return smoothTemporal(packPolygon(ring, imageW, imageH), 0.48f, 1);
    }

    std::vector<Vec2> ring;
    ring.reserve(48);
    const int segments = 44;
    for (int i = 0; i < segments; ++i) {
        const float t = static_cast<float>(i) / static_cast<float>(segments) * 2.f * kPi;
        ring.push_back({cx + rx * std::cos(t), cy + ry * std::sin(t)});
    }
    smoothChaikinClosed(ring, 1);
    return smoothTemporal(packPolygon(ring, imageW, imageH), 0.52f, 1);
}

ContourPacket contourFromObjectBox(
    float left,
    float top,
    float right,
    float bottom,
    int imageW,
    int imageH) {
    const float w = right - left;
    const float h = bottom - top;
    const float r = std::min(w, h) * 0.18f;
    const float cxL = left + r;
    const float cxR = right - r;
    const float cyT = top + r;
    const float cyB = bottom - r;

    std::vector<Vec2> ring;
    ring.reserve(52);
    const int cornerSegs = 10;
    pushArc(ring, {cxR, cyT}, r, -kPi * 0.5f, 0.f, cornerSegs);
    pushArc(ring, {cxR, cyB}, r, 0.f, kPi * 0.5f, cornerSegs);
    pushArc(ring, {cxL, cyB}, r, kPi * 0.5f, kPi, cornerSegs);
    pushArc(ring, {cxL, cyT}, r, kPi, kPi * 1.5f, cornerSegs);
    smoothChaikinClosed(ring, 1);
    return smoothTemporal(packPolygon(ring, imageW, imageH), 0.38f, 2);
}

ContourPacket smoothTemporal(const ContourPacket& incoming, float alpha, int channel) {
    NativeLock guard;
    ContourPacket out = incoming;
    const int ch = std::max(0, std::min(channel, kSmoothChannels - 1));

    if (incoming.norm.size() < 3) {
        g_prevNorm[ch].clear();
        g_hasPrev[ch] = false;
        return out;
    }

    const float inArea = normBoundsArea(incoming.bounds);
    const float maxNormArea = (ch == 1) ? kMaxFaceNormArea : kMaxPersonNormArea;
    if (inArea > maxNormArea + 0.08f && g_hasLastGood[ch]) {
        ContourPacket held = g_lastGoodPkt[ch];
        held.bounds = g_prevBounds[ch];
        if (!g_prevNorm[ch].empty()) {
            held.norm = g_prevNorm[ch];
        }
        return held;
    }

    std::vector<Vec2> current = resampleUniformArc(incoming.norm, kTemporalVerts);

    if (g_hasPrev[ch] && g_prevNorm[ch].size() == current.size()) {
        const float jumpX = boundsCenterX(incoming.bounds) - boundsCenterX(g_prevBounds[ch]);
        const float jumpY = boundsCenterY(incoming.bounds) - boundsCenterY(g_prevBounds[ch]);
        const float jump = std::sqrt(jumpX * jumpX + jumpY * jumpY);
        const float prevArea = normBoundsArea(g_prevBounds[ch]);
        const float areaRatio = prevArea > 1e-4f ? inArea / prevArea : 1.f;
        const float baseAlpha = (ch == 1) ? std::max(alpha, 0.48f) : alpha;
        const float jumpScale = (ch == 1) ? 1.25f : 1.f;
        float a = adaptiveTemporalAlpha(baseAlpha, jump * jumpScale, areaRatio);
        if (ch == 1) {
            a = std::min(0.88f, a * 1.06f);
        }

        alignRingCyclic(current, g_prevNorm[ch]);
        for (size_t i = 0; i < current.size(); ++i) {
            current[i].x = g_prevNorm[ch][i].x + (current[i].x - g_prevNorm[ch][i].x) * a;
            current[i].y = g_prevNorm[ch][i].y + (current[i].y - g_prevNorm[ch][i].y) * a;
        }
        out.norm = current;
        out.bounds.left = g_prevBounds[ch].left + (incoming.bounds.left - g_prevBounds[ch].left) * a;
        out.bounds.top = g_prevBounds[ch].top + (incoming.bounds.top - g_prevBounds[ch].top) * a;
        out.bounds.right = g_prevBounds[ch].right + (incoming.bounds.right - g_prevBounds[ch].right) * a;
        out.bounds.bottom = g_prevBounds[ch].bottom + (incoming.bounds.bottom - g_prevBounds[ch].bottom) * a;
    } else {
        out.norm = current;
    }

    g_prevNorm[ch] = out.norm;
    g_prevBounds[ch] = out.bounds;
    g_hasPrev[ch] = true;
    if (normBoundsArea(out.bounds) <= maxNormArea) {
        g_lastGoodPkt[ch] = out;
        g_hasLastGood[ch] = true;
    }
    return out;
}

void resetTemporalSmoothing() {
    NativeLock guard;
    for (int i = 0; i < kSmoothChannels; ++i) {
        g_prevNorm[i].clear();
        g_hasPrev[i] = false;
        g_hasLastGood[i] = false;
    }
}

bool bandInterval(
    const std::vector<Vec2>& polyPx,
    float bandTop,
    float bandBottom,
    float& outMinX,
    float& outMaxX) {
    const size_t n = polyPx.size();
    if (n < 2) return false;
    float minX = 1e30f;
    float maxX = -1e30f;
    bool found = false;
    for (size_t i = 0; i < n; ++i) {
        const Vec2& p0 = polyPx[i];
        const Vec2& p1 = polyPx[(i + 1) % n];
        if (p0.y >= bandTop && p0.y <= bandBottom) {
            minX = std::min(minX, p0.x);
            maxX = std::max(maxX, p0.x);
            found = true;
        }
        const float yMin = std::min(p0.y, p1.y);
        const float yMax = std::max(p0.y, p1.y);
        if (yMax >= bandTop && yMin <= bandBottom && std::fabs(p1.y - p0.y) > 1e-5f) {
            const float clips[2] = {bandTop, bandBottom};
            for (float yClip : clips) {
                if (yClip >= yMin && yClip <= yMax) {
                    const float t = (yClip - p0.y) / (p1.y - p0.y);
                    const float xClip = p0.x + t * (p1.x - p0.x);
                    minX = std::min(minX, xClip);
                    maxX = std::max(maxX, xClip);
                    found = true;
                }
            }
        }
    }
    if (!found || maxX <= minX) return false;
    outMinX = minX;
    outMaxX = maxX;
    return true;
}

}
