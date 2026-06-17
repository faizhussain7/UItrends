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
    pretext_ncnn::Detection det;
    if (!pretext_ncnn::detectBest(rgb, rgbW, rgbH, &det,  true)) return false;

    pretext::ContourPacket pkt = pretext::contourFromObjectBox(
        det.left, det.top, det.right, det.bottom, imageW, imageH);
    if (pkt.norm.size() < 3) return false;

    out->source = VisionSourceId::Object;
    out->classId = det.classId;
    out->score = det.score;
    out->contour = std::move(pkt);
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
