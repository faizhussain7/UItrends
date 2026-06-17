/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

#include "pretext_geometry.h"

#include <cstddef>
#include <vector>

namespace pretext {
namespace contour {

constexpr float kPi = 3.14159265358979323846f;

float clampf(float v, float lo, float hi);
Vec2 lerp(const Vec2& a, const Vec2& b, float t);
float len(const Vec2& v);
Vec2 norm(const Vec2& v);
float distPointToSegment(const Vec2& p, const Vec2& a, const Vec2& b);

ContourPacket packPolygon(const std::vector<Vec2>& px, int imageW, int imageH);
std::vector<Vec2> normToPixels(const ContourPacket& pkt, int imageW, int imageH);

std::vector<Vec2> simplifyClosedRdp(std::vector<Vec2> ring, float epsilonPx, int maxVerts);
std::vector<Vec2> resampleUniformArc(const std::vector<Vec2>& ring, int targetCount);
void alignRingCyclic(std::vector<Vec2>& ring, const std::vector<Vec2>& reference);
void smoothChaikinClosed(std::vector<Vec2>& ring, int iterations);

bool pointInsidePolygon(const std::vector<Vec2>& ring, const Vec2& p);
void insertVertexOnRing(std::vector<Vec2>& ring, const Vec2& p);

float computeOtsuIsoLevel(const float* mask, int total);


float computeAdaptiveIsoLevel(const float* mask, int total, int w, int h);

void keepLargestForegroundBlob(std::vector<uint8_t>& bin, int w, int h);


void keepBestForegroundBlob(std::vector<uint8_t>& bin, int w, int h);

float normBoundsArea(const Bounds& b);


float shoelaceAreaAbs(const std::vector<Vec2>& ring);


float convexHullAreaAbs(const std::vector<Vec2>& points);


float contourSolidity(const std::vector<Vec2>& ring);


std::vector<Vec2> extractBestPersonIsoContour(const float* mask, int w, int h);


std::vector<Vec2> marchingSquaresIsoContour(const float* mask, int w, int h, float iso);


float adaptiveTemporalAlpha(float baseAlpha, float jumpNorm, float areaRatio);


void boxBlurMask3x3(std::vector<float>& mask, int w, int h);


std::vector<float> downsampleMaskNearest(
    const float* mask,
    int inW,
    int inH,
    int outW,
    int outH);

}
}
