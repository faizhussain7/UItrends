/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

#include <cstdint>

namespace pretext {
namespace vision {


void yuv420ToRgbRotate(
    const uint8_t* y,
    int yRowStride,
    const uint8_t* u,
    int uRowStride,
    int uPixelStride,
    const uint8_t* v,
    int vRowStride,
    int vPixelStride,
    int width,
    int height,
    int rotationDegrees,
    uint8_t* outRgb,
    int* outW,
    int* outH);

}
}
