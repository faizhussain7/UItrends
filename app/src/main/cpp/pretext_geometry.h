/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

#include <cstddef>
#include <cstdint>
#include <vector>

namespace pretext {

struct Vec2 {
    float x = 0.f;
    float y = 0.f;
};

inline Vec2 operator+(const Vec2& a, const Vec2& b) {
    return {a.x + b.x, a.y + b.y};
}

inline Vec2 operator-(const Vec2& a, const Vec2& b) {
    return {a.x - b.x, a.y - b.y};
}

inline Vec2 operator*(const Vec2& v, float s) {
    return {v.x * s, v.y * s};
}

inline Vec2 operator*(float s, const Vec2& v) {
    return v * s;
}

struct Bounds {
    float left = 0.f;
    float top = 0.f;
    float right = 0.f;
    float bottom = 0.f;
};

struct ContourPacket {
    Bounds bounds{};
    std::vector<Vec2> norm{};
};

constexpr int kMaxContourVerts = 96;
constexpr int kTemporalVerts = 48;
constexpr int kSmoothChannels = 3;
constexpr float kMinLikelihood = 0.32f;
constexpr float kMaskForegroundMin = 0.08f;
constexpr float kDefaultMaskThreshold = 0.42f;

constexpr float kMaxPersonNormArea = 0.68f;
constexpr float kMaxFaceNormArea = 0.20f;
constexpr float kMaxObjectNormArea = 0.52f;


ContourPacket contourFromSegmentationMask(
    const float* mask,
    int maskW,
    int maskH,
    int imageW,
    int imageH);

ContourPacket extractObjectContourFromMaskCrop(
    const float* maskCrop,
    int cropW,
    int cropH,
    float offsetX,
    float offsetY,
    int imageW,
    int imageH,
    bool temporalSmooth = false);


ContourPacket extractPersonContour(
    const float* mask,
    int maskW,
    int maskH,
    const float* landmarks,
    int landmarkCount,
    int imageW,
    int imageH);


ContourPacket extractFaceContour(
    const float* polylineXy,
    int pointCount,
    float cx,
    float cy,
    float rx,
    float ry,
    int imageW,
    int imageH);

ContourPacket smoothTemporal(const ContourPacket& incoming, float alpha, int channel);
void resetTemporalSmoothing();

bool bandInterval(
    const std::vector<Vec2>& polyPx,
    float bandTop,
    float bandBottom,
    float& outMinX,
    float& outMaxX);

}
