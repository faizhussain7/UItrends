/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_yuv_rgb.h"

#include <algorithm>

namespace pretext {
namespace vision {
namespace {

static inline int clamp255(int v) {
    return std::max(0, std::min(255, v));
}

static inline void yuvToRgbPixel(int y, int u, int v, uint8_t* rgb) {
    const int yy = std::max(0, y - 16);
    const int uu = u - 128;
    const int vv = v - 128;
    const int y2 = yy * 1192;
    const int r = clamp255((y2 + 1634 * vv) >> 10);
    const int g = clamp255((y2 - 833 * vv - 400 * uu) >> 10);
    const int b = clamp255((y2 + 2066 * uu) >> 10);
    rgb[0] = static_cast<uint8_t>(r);
    rgb[1] = static_cast<uint8_t>(g);
    rgb[2] = static_cast<uint8_t>(b);
}

}

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
    int* outH) {
    if (!y || !u || !v || !outRgb || width <= 0 || height <= 0) {
        if (outW) *outW = 0;
        if (outH) *outH = 0;
        return;
    }

    const bool rot90 = rotationDegrees == 90 || rotationDegrees == 270;
    const int dstW = rot90 ? height : width;
    const int dstH = rot90 ? width : height;
    if (outW) *outW = dstW;
    if (outH) *outH = dstH;

    auto writePixel = [&](int srcX, int srcY, int dstX, int dstY) {
        const int yIndex = srcY * yRowStride + srcX;
        const int uvRow = (srcY >> 1);
        const int uvCol = (srcX >> 1) * uPixelStride;
        const int uIndex = uvRow * uRowStride + uvCol;
        const int vIndex = uvRow * vRowStride + uvCol;
        const int Y = static_cast<int>(y[yIndex]);
        const int U = static_cast<int>(u[uIndex]);
        const int V = static_cast<int>(v[vIndex]);
        uint8_t* dst = outRgb + (dstY * dstW + dstX) * 3;
        yuvToRgbPixel(Y, U, V, dst);
    };

    switch (rotationDegrees) {
        case 90:
            for (int sy = 0; sy < height; ++sy) {
                for (int sx = 0; sx < width; ++sx) {
                    const int dx = height - 1 - sy;
                    const int dy = sx;
                    writePixel(sx, sy, dx, dy);
                }
            }
            break;
        case 180:
            for (int sy = 0; sy < height; ++sy) {
                for (int sx = 0; sx < width; ++sx) {
                    const int dx = width - 1 - sx;
                    const int dy = height - 1 - sy;
                    writePixel(sx, sy, dx, dy);
                }
            }
            break;
        case 270:
            for (int sy = 0; sy < height; ++sy) {
                for (int sx = 0; sx < width; ++sx) {
                    const int dx = sy;
                    const int dy = width - 1 - sx;
                    writePixel(sx, sy, dx, dy);
                }
            }
            break;
        default:
            for (int sy = 0; sy < height; ++sy) {
                for (int sx = 0; sx < width; ++sx) {
                    writePixel(sx, sy, sx, sy);
                }
            }
            break;
    }
}

}
}
