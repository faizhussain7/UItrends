/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

#include <android/asset_manager.h>

namespace pretext_ncnn {

struct Detection {
    float left = 0.f;
    float top = 0.f;
    float right = 0.f;
    float bottom = 0.f;
    float score = 0.f;
    int classId = -1;
};

bool init(AAssetManager* mgr);
void release();


bool detectBest(
    const unsigned char* rgb,
    int width,
    int height,
    Detection* out,
    bool excludePerson = false);

const char* classLabel(int classId);

}
