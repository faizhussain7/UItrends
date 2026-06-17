/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_contour_internal.h"

#include <algorithm>
#include <cmath>
#include <cstdint>

namespace pretext {
namespace contour {

float clampf(float v, float lo, float hi) {
    return v < lo ? lo : (v > hi ? hi : v);
}

Vec2 lerp(const Vec2& a, const Vec2& b, float t) {
    return {a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t};
}

float len(const Vec2& v) {
    return std::sqrt(v.x * v.x + v.y * v.y);
}

Vec2 norm(const Vec2& v) {
    const float l = len(v);
    return l > 1e-5f ? Vec2{v.x / l, v.y / l} : Vec2{0.f, 0.f};
}

float distPointToSegment(const Vec2& p, const Vec2& a, const Vec2& b) {
    const float abx = b.x - a.x;
    const float aby = b.y - a.y;
    const float abLen2 = abx * abx + aby * aby;
    if (abLen2 < 1e-5f) return len(Vec2{p.x - a.x, p.y - a.y});
    const float t = clampf(((p.x - a.x) * abx + (p.y - a.y) * aby) / abLen2, 0.f, 1.f);
    const float qx = a.x + abx * t;
    const float qy = a.y + aby * t;
    return std::sqrt((p.x - qx) * (p.x - qx) + (p.y - qy) * (p.y - qy));
}

static Bounds boundsOf(const std::vector<Vec2>& pts, int imageW, int imageH) {
    Bounds b{1.f, 1.f, 0.f, 0.f};
    if (pts.empty()) return b;
    float minX = pts[0].x;
    float minY = pts[0].y;
    float maxX = pts[0].x;
    float maxY = pts[0].y;
    for (const Vec2& p : pts) {
        minX = std::min(minX, p.x);
        minY = std::min(minY, p.y);
        maxX = std::max(maxX, p.x);
        maxY = std::max(maxY, p.y);
    }
    const float w = static_cast<float>(imageW);
    const float h = static_cast<float>(imageH);
    b.left = clampf(minX / w, 0.f, 1.f);
    b.top = clampf(minY / h, 0.f, 1.f);
    b.right = clampf(maxX / w, b.left + 1e-4f, 1.f);
    b.bottom = clampf(maxY / h, b.top + 1e-4f, 1.f);
    return b;
}

ContourPacket packPolygon(const std::vector<Vec2>& px, int imageW, int imageH) {
    ContourPacket pkt;
    pkt.bounds = boundsOf(px, imageW, imageH);
    pkt.norm.reserve(px.size());
    const float w = static_cast<float>(imageW);
    const float h = static_cast<float>(imageH);
    for (const Vec2& p : px) {
        pkt.norm.push_back({clampf(p.x / w, 0.f, 1.f), clampf(p.y / h, 0.f, 1.f)});
    }
    return pkt;
}

std::vector<Vec2> normToPixels(const ContourPacket& pkt, int imageW, int imageH) {
    const float w = static_cast<float>(imageW);
    const float h = static_cast<float>(imageH);
    std::vector<Vec2> px;
    px.reserve(pkt.norm.size());
    for (const Vec2& p : pkt.norm) {
        px.push_back({p.x * w, p.y * h});
    }
    return px;
}

static void rdpOpenRecurse(
    const std::vector<Vec2>& pts,
    size_t i0,
    size_t i1,
    float epsilon,
    std::vector<uint8_t>& keep) {
    if (i1 <= i0 + 1) return;
    float maxD = 0.f;
    size_t idx = i0;
    for (size_t i = i0 + 1; i < i1; ++i) {
        const float d = distPointToSegment(pts[i], pts[i0], pts[i1]);
        if (d > maxD) {
            maxD = d;
            idx = i;
        }
    }
    if (maxD > epsilon) {
        keep[idx] = 1;
        rdpOpenRecurse(pts, i0, idx, epsilon, keep);
        rdpOpenRecurse(pts, idx, i1, epsilon, keep);
    }
}

std::vector<Vec2> simplifyClosedRdp(std::vector<Vec2> ring, float epsilonPx, int maxVerts) {
    if (ring.size() < 4) return ring;
    if (len(ring.front() - ring.back()) > 0.5f) ring.push_back(ring.front());

    std::vector<uint8_t> keep(ring.size(), 0);
    keep[0] = 1;
    keep[ring.size() - 1] = 1;
    rdpOpenRecurse(ring, 0, ring.size() - 1, epsilonPx, keep);

    std::vector<Vec2> out;
    out.reserve(ring.size());
    for (size_t i = 0; i < ring.size(); ++i) {
        if (keep[i]) out.push_back(ring[i]);
    }
    if (out.size() > 1 && len(out.front() - out.back()) < 0.5f) out.pop_back();

    if (static_cast<int>(out.size()) <= maxVerts) return out;

    std::vector<Vec2> capped;
    capped.reserve(static_cast<size_t>(maxVerts));
    const int step = std::max(static_cast<int>(out.size()) / maxVerts, 1);
    for (size_t i = 0; i < out.size(); i += static_cast<size_t>(step)) {
        capped.push_back(out[i]);
    }
    return capped;
}

std::vector<Vec2> resampleUniformArc(const std::vector<Vec2>& ring, int targetCount) {
    if (ring.size() < 3 || targetCount < 3) return ring;
    std::vector<float> cum;
    cum.reserve(ring.size() + 1);
    cum.push_back(0.f);
    float total = 0.f;
    for (size_t i = 0; i < ring.size(); ++i) {
        total += len(ring[(i + 1) % ring.size()] - ring[i]);
        cum.push_back(total);
    }
    if (total < 1e-3f) return ring;

    std::vector<Vec2> out;
    out.reserve(static_cast<size_t>(targetCount));
    for (int i = 0; i < targetCount; ++i) {
        const float target = total * static_cast<float>(i) / static_cast<float>(targetCount);
        size_t seg = 0;
        while (seg + 1 < cum.size() && cum[seg + 1] < target) ++seg;
        const float segLen = cum[seg + 1] - cum[seg];
        const float t = segLen > 1e-5f ? (target - cum[seg]) / segLen : 0.f;
        const Vec2& a = ring[seg % ring.size()];
        const Vec2& b = ring[(seg + 1) % ring.size()];
        out.push_back(lerp(a, b, t));
    }
    return out;
}

void alignRingCyclic(std::vector<Vec2>& ring, const std::vector<Vec2>& reference) {
    const int n = static_cast<int>(ring.size());
    if (n < 3 || n != static_cast<int>(reference.size())) return;

    int bestShift = 0;
    float bestDist = 1e30f;
    for (int shift = 0; shift < n; ++shift) {
        float dist = 0.f;
        for (int i = 0; i < n; ++i) {
            const Vec2& a = ring[static_cast<size_t>((i + shift) % n)];
            const Vec2& b = reference[static_cast<size_t>(i)];
            dist += len(a - b);
        }
        if (dist < bestDist) {
            bestDist = dist;
            bestShift = shift;
        }
    }
    if (bestShift == 0) return;

    std::vector<Vec2> rotated(static_cast<size_t>(n));
    for (int i = 0; i < n; ++i) {
        rotated[static_cast<size_t>(i)] = ring[static_cast<size_t>((i + bestShift) % n)];
    }
    ring.swap(rotated);
}

void smoothChaikinClosed(std::vector<Vec2>& ring, int iterations) {
    for (int it = 0; it < iterations; ++it) {
        if (ring.size() < 3) return;
        std::vector<Vec2> next;
        next.reserve(ring.size() * 2);
        const size_t n = ring.size();
        for (size_t i = 0; i < n; ++i) {
            const Vec2& p0 = ring[i];
            const Vec2& p1 = ring[(i + 1) % n];
            next.push_back(lerp(p0, p1, 0.25f));
            next.push_back(lerp(p0, p1, 0.75f));
        }
        ring.swap(next);
    }
}

bool pointInsidePolygon(const std::vector<Vec2>& ring, const Vec2& p) {
    bool inside = false;
    const size_t n = ring.size();
    for (size_t i = 0, j = n - 1; i < n; j = i++) {
        const Vec2& a = ring[i];
        const Vec2& b = ring[j];
        const bool hit =
            ((a.y > p.y) != (b.y > p.y)) &&
            (p.x < (b.x - a.x) * (p.y - a.y) / (b.y - a.y + 1e-6f) + a.x);
        if (hit) inside = !inside;
    }
    return inside;
}

float shoelaceAreaAbs(const std::vector<Vec2>& ring) {
    const size_t n = ring.size();
    if (n < 3) return 0.f;
    double sum = 0.0;
    for (size_t i = 0; i < n; ++i) {
        const Vec2& a = ring[i];
        const Vec2& b = ring[(i + 1) % n];
        sum += static_cast<double>(a.x) * static_cast<double>(b.y) -
               static_cast<double>(b.x) * static_cast<double>(a.y);
    }
    return static_cast<float>(std::fabs(sum) * 0.5);
}

static std::vector<Vec2> convexHullMonotoneChain(std::vector<Vec2> pts) {
    if (pts.size() < 3) return pts;
    std::sort(pts.begin(), pts.end(), [](const Vec2& a, const Vec2& b) {
        return a.x < b.x || (a.x == b.x && a.y < b.y);
    });
    auto cross = [](const Vec2& o, const Vec2& a, const Vec2& b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    };
    std::vector<Vec2> lower;
    for (const Vec2& p : pts) {
        while (lower.size() >= 2 &&
               cross(lower[lower.size() - 2], lower[lower.size() - 1], p) <= 0.f) {
            lower.pop_back();
        }
        lower.push_back(p);
    }
    std::vector<Vec2> upper;
    for (auto it = pts.rbegin(); it != pts.rend(); ++it) {
        const Vec2& p = *it;
        while (upper.size() >= 2 &&
               cross(upper[upper.size() - 2], upper[upper.size() - 1], p) <= 0.f) {
            upper.pop_back();
        }
        upper.push_back(p);
    }
    lower.pop_back();
    upper.pop_back();
    lower.insert(lower.end(), upper.begin(), upper.end());
    return lower;
}

float convexHullAreaAbs(const std::vector<Vec2>& points) {
    if (points.size() < 3) return 0.f;
    return shoelaceAreaAbs(convexHullMonotoneChain(points));
}

float contourSolidity(const std::vector<Vec2>& ring) {
    const float area = shoelaceAreaAbs(ring);
    const float hull = convexHullAreaAbs(ring);
    if (hull < 1e-3f) return 0.f;
    return clampf(area / hull, 0.f, 1.f);
}

float adaptiveTemporalAlpha(float baseAlpha, float jumpNorm, float areaRatio) {
    const float speed = jumpNorm * 12.f;
    const float cutoff = 1.15f + 0.55f * speed;
    const float dt = 1.f / 12.f;
    const float tau = 1.f / (2.f * kPi * cutoff);
    const float lowPass = 1.f / (1.f + tau / dt);
    float a = clampf(baseAlpha * lowPass, 0.12f, 0.88f);
    if (areaRatio > 2.4f || areaRatio < 0.32f) {
        a = std::min(a, 0.28f);
    } else if (jumpNorm < 0.025f) {
        a = std::max(a, baseAlpha * 0.92f);
    }
    return a;
}

void insertVertexOnRing(std::vector<Vec2>& ring, const Vec2& p) {
    if (ring.size() < 2) {
        ring.push_back(p);
        return;
    }
    size_t bestEdge = 0;
    float bestDist = 1e30f;
    for (size_t i = 0; i < ring.size(); ++i) {
        const float d = distPointToSegment(p, ring[i], ring[(i + 1) % ring.size()]);
        if (d < bestDist) {
            bestDist = d;
            bestEdge = i;
        }
    }
    ring.insert(ring.begin() + static_cast<std::ptrdiff_t>(bestEdge + 1), p);
}

}
}
