/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include <jni.h>

#include <vector>

#include "pretext_geometry.h"

namespace {

static jfloatArray makePacket(JNIEnv* env, const pretext::ContourPacket& pkt) {
    const int n = static_cast<int>(pkt.norm.size());
    const int len = 5 + n * 2;
    jfloatArray arr = env->NewFloatArray(len);
    if (!arr) return nullptr;
    std::vector<float> buf(static_cast<size_t>(len));
    buf[0] = pkt.bounds.left;
    buf[1] = pkt.bounds.top;
    buf[2] = pkt.bounds.right;
    buf[3] = pkt.bounds.bottom;
    buf[4] = static_cast<float>(n);
    for (int i = 0; i < n; ++i) {
        buf[5 + i * 2] = pkt.norm[static_cast<size_t>(i)].x;
        buf[5 + i * 2 + 1] = pkt.norm[static_cast<size_t>(i)].y;
    }
    env->SetFloatArrayRegion(arr, 0, len, buf.data());
    return arr;
}

static pretext::ContourPacket readPacket(const float* data, int len) {
    pretext::ContourPacket pkt;
    if (!data || len < 5) return pkt;
    pkt.bounds.left = data[0];
    pkt.bounds.top = data[1];
    pkt.bounds.right = data[2];
    pkt.bounds.bottom = data[3];
    const int n = static_cast<int>(data[4]);
    if (n <= 0 || 5 + n * 2 > len) return pkt;
    pkt.norm.reserve(static_cast<size_t>(n));
    for (int i = 0; i < n; ++i) {
        pkt.norm.push_back({data[5 + i * 2], data[5 + i * 2 + 1]});
    }
    return pkt;
}

}

extern "C" {

JNIEXPORT jfloatArray JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeGeometry_nativeExtractPerson(
    JNIEnv* env,
    jclass,
    jfloatArray mask,
    jint maskW,
    jint maskH,
    jfloatArray landmarks,
    jint imageW,
    jint imageH) {
    const jsize maskLen = env->GetArrayLength(mask);
    if (maskLen < maskW * maskH) return nullptr;
    std::vector<float> maskBuf(static_cast<size_t>(maskLen));
    env->GetFloatArrayRegion(mask, 0, maskLen, maskBuf.data());

    const float* lmPtr = nullptr;
    int lmCount = 0;
    std::vector<float> lmBuf;
    if (landmarks != nullptr) {
        const jsize lmLen = env->GetArrayLength(landmarks);
        if (lmLen >= 30) {
            lmBuf.resize(static_cast<size_t>(lmLen));
            env->GetFloatArrayRegion(landmarks, 0, lmLen, lmBuf.data());
            lmPtr = lmBuf.data();
            lmCount = static_cast<int>(lmLen / 3);
        }
    }

    pretext::ContourPacket pkt = pretext::extractPersonContour(
        maskBuf.data(), maskW, maskH, lmPtr, lmCount, imageW, imageH);
    if (pkt.norm.size() < 3) return nullptr;
    return makePacket(env, pkt);
}

JNIEXPORT jfloatArray JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeGeometry_nativeExtractFace(
    JNIEnv* env,
    jclass,
    jfloatArray polyline,
    jfloat cx,
    jfloat cy,
    jfloat rx,
    jfloat ry,
    jint imageW,
    jint imageH) {
    const float* polyPtr = nullptr;
    int pointCount = 0;
    std::vector<float> polyBuf;
    if (polyline != nullptr) {
        const jsize len = env->GetArrayLength(polyline);
        if (len >= 12) {
            polyBuf.resize(static_cast<size_t>(len));
            env->GetFloatArrayRegion(polyline, 0, len, polyBuf.data());
            polyPtr = polyBuf.data();
            pointCount = static_cast<int>(len / 2);
        }
    }
    pretext::ContourPacket pkt = pretext::extractFaceContour(
        polyPtr, pointCount, cx, cy, rx, ry, imageW, imageH);
    if (pkt.norm.size() < 3) return nullptr;
    return makePacket(env, pkt);
}

JNIEXPORT jfloatArray JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeGeometry_nativeExtractObject(
    JNIEnv* env,
    jclass,
    jfloat left,
    jfloat top,
    jfloat right,
    jfloat bottom,
    jint imageW,
    jint imageH) {
    pretext::ContourPacket pkt = pretext::contourFromObjectBox(
        left, top, right, bottom, imageW, imageH);
    if (pkt.norm.size() < 3) return nullptr;
    return makePacket(env, pkt);
}

JNIEXPORT jfloatArray JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeGeometry_nativeBandInterval(
    JNIEnv* env,
    jclass,
    jfloatArray polyPx,
    jfloat bandTop,
    jfloat bandBottom) {
    const jsize len = env->GetArrayLength(polyPx);
    if (len < 3) return nullptr;
    std::vector<float> buf(static_cast<size_t>(len));
    env->GetFloatArrayRegion(polyPx, 0, len, buf.data());
    const int n = static_cast<int>(buf[0]);
    if (n < 2 || 1 + n * 2 > len) return nullptr;
    std::vector<pretext::Vec2> poly(static_cast<size_t>(n));
    for (int i = 0; i < n; ++i) {
        poly[static_cast<size_t>(i)] = {buf[static_cast<size_t>(1 + i * 2)], buf[static_cast<size_t>(1 + i * 2 + 1)]};
    }
    float minX = 0.f;
    float maxX = 0.f;
    if (!pretext::bandInterval(poly, bandTop, bandBottom, minX, maxX)) return nullptr;
    jfloatArray out = env->NewFloatArray(2);
    if (!out) return nullptr;
    const float vals[2] = {minX, maxX};
    env->SetFloatArrayRegion(out, 0, 2, vals);
    return out;
}

JNIEXPORT void JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeGeometry_nativeResetSmoothing(JNIEnv*, jclass) {
    pretext::resetTemporalSmoothing();
}

}
