/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

#include <cstdint>

#include "pretext_geometry.h"

namespace pretext {
namespace vision {

enum class VisionSourceId : int {
    Face = 0,
    Person = 1,
    Object = 2,
};

struct VisionPacket {
    VisionSourceId source = VisionSourceId::Object;
    int classId = -1;
    float score = 0.f;
    pretext::ContourPacket contour{};
};

bool processObjectFromRgb(
    const uint8_t* rgb,
    int rgbW,
    int rgbH,
    int imageW,
    int imageH,
    VisionPacket* out);

}
}
