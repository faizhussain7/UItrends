/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_vision_orchestrator.h"

#ifdef PRETEXT_USE_NCNN
#include "pretext_ncnn_yolo.h"
#endif

namespace pretext {
namespace vision {

bool processObjectFromRgb(
    const uint8_t* rgb,
    int rgbW,
    int rgbH,
    int imageW,
    int imageH,
    VisionPacket* out) {
    if (!rgb || rgbW <= 0 || rgbH <= 0 || imageW <= 0 || imageH <= 0 || !out) return false;

#ifdef PRETEXT_USE_NCNN
    if (!pretext_ncnn::isSegmentationBackend()) {
        return false;
    }
    pretext_ncnn::DetectionWithContour dc;
    int count = 0;
    if (!pretext_ncnn::detectTopKWithContours(rgb, rgbW, rgbH, &dc, 1, &count, true) ||
        count <= 0) {
        return false;
    }
    if (dc.contour.norm.size() < 6) return false;

    out->source = VisionSourceId::Object;
    out->classId = dc.det.classId;
    out->score = dc.det.score;
    out->contour = std::move(dc.contour);
    return true;
#else
    (void)rgb;
    (void)rgbW;
    (void)rgbH;
    (void)imageW;
    (void)imageH;
    return false;
#endif
}

}
}
