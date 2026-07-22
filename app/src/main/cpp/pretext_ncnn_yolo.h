/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

#include <android/asset_manager.h>

#include "pretext_geometry.h"

namespace pretext_ncnn {

struct Detection {
    float left = 0.f;
    float top = 0.f;
    float right = 0.f;
    float bottom = 0.f;
    float score = 0.f;
    int classId = -1;
};

struct DetectionWithContour {
    Detection det{};
    pretext::ContourPacket contour{};
};

bool init(AAssetManager* mgr);
void release();

void setHighQuality(bool high);
bool isSegmentationBackend();
bool isHighQuality();

bool detectTopKWithContours(
    const unsigned char* rgb,
    int width,
    int height,
    DetectionWithContour* out,
    int maxK,
    int* outCount,
    bool excludePerson = false);

const char* classLabel(int classId);

}
