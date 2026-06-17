/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include <jni.h>

#include <android/asset_manager_jni.h>

#include <vector>

#include "pretext_geometry.h"
#include "pretext_yuv_rgb.h"
#include "pretext_vision_orchestrator.h"

#ifdef PRETEXT_USE_NCNN
#include "pretext_ncnn_yolo.h"
#endif

namespace {

static jfloatArray makeVisionPacket(JNIEnv* env, const pretext::vision::VisionPacket& p) {
    const int n = static_cast<int>(p.contour.norm.size());
    if (n < 3) return nullptr;

    const int len = 8 + n * 2;
    jfloatArray arr = env->NewFloatArray(len);
    if (!arr) return nullptr;

    std::vector<float> buf(static_cast<size_t>(len));
    buf[0] = static_cast<float>(static_cast<int>(p.source));
    buf[1] = static_cast<float>(p.classId);
    buf[2] = p.score;
    buf[3] = p.contour.bounds.left;
    buf[4] = p.contour.bounds.top;
    buf[5] = p.contour.bounds.right;
    buf[6] = p.contour.bounds.bottom;
    buf[7] = static_cast<float>(n);
    for (int i = 0; i < n; ++i) {
        buf[8 + i * 2] = p.contour.norm[static_cast<size_t>(i)].x;
        buf[8 + i * 2 + 1] = p.contour.norm[static_cast<size_t>(i)].y;
    }
    env->SetFloatArrayRegion(arr, 0, len, buf.data());
    return arr;
}

}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeVision_nativeInit(
    JNIEnv* env,
    jclass,
    jobject assetManager) {
    if (!assetManager) return JNI_FALSE;
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    if (!mgr) return JNI_FALSE;
#ifdef PRETEXT_USE_NCNN
    return pretext_ncnn::init(mgr) ? JNI_TRUE : JNI_FALSE;
#else
    (void)mgr;
    return JNI_FALSE;
#endif
}

JNIEXPORT void JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeVision_nativeRelease(JNIEnv*, jclass) {
#ifdef PRETEXT_USE_NCNN
    pretext_ncnn::release();
#endif
}

JNIEXPORT jfloatArray JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeVision_nativeProcessFrame(
    JNIEnv* env,
    jclass,
    jint mode,
    jint rotationDegrees,
    jint imageW,
    jint imageH,
    jobject yBuf,
    jint yRowStride,
    jobject uBuf,
    jint uRowStride,
    jint uPixelStride,
    jobject vBuf,
    jint vRowStride,
    jint vPixelStride) {
    if (!yBuf || !uBuf || !vBuf) return nullptr;
    auto* y = static_cast<const uint8_t*>(env->GetDirectBufferAddress(yBuf));
    auto* u = static_cast<const uint8_t*>(env->GetDirectBufferAddress(uBuf));
    auto* v = static_cast<const uint8_t*>(env->GetDirectBufferAddress(vBuf));
    if (!y || !u || !v) return nullptr;

    (void)mode;

    const int srcW = static_cast<int>(imageW);
    const int srcH = static_cast<int>(imageH);
    if (srcW <= 0 || srcH <= 0) return nullptr;

    const bool rot90 = rotationDegrees == 90 || rotationDegrees == 270;
    const int rgbW = rot90 ? srcH : srcW;
    const int rgbH = rot90 ? srcW : srcH;
    thread_local std::vector<uint8_t> rgb;
    rgb.resize(static_cast<size_t>(rgbW * rgbH * 3));

    int outW = 0;
    int outH = 0;
    pretext::vision::yuv420ToRgbRotate(
        y,
        yRowStride,
        u,
        uRowStride,
        uPixelStride,
        v,
        vRowStride,
        vPixelStride,
        srcW,
        srcH,
        rotationDegrees,
        rgb.data(),
        &outW,
        &outH);
    if (outW != rgbW || outH != rgbH) return nullptr;

    pretext::vision::VisionPacket pkt;
    if (!pretext::vision::processObjectFromRgb(rgb.data(), rgbW, rgbH, rgbW, rgbH, &pkt)) {
        return nullptr;
    }
    return makeVisionPacket(env, pkt);
}

JNIEXPORT jfloatArray JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeVision_nativeDetectBestRgb(
    JNIEnv* env,
    jclass,
    jbyteArray rgb,
    jint width,
    jint height,
    jboolean excludePerson) {
#ifdef PRETEXT_USE_NCNN
    if (!rgb) return nullptr;
    const jsize len = env->GetArrayLength(rgb);
    if (len < width * height * 3) return nullptr;
    jbyte* bytes = env->GetByteArrayElements(rgb, nullptr);
    if (!bytes) return nullptr;

    pretext_ncnn::Detection det;
    const bool ok = pretext_ncnn::detectBest(
        reinterpret_cast<const unsigned char*>(bytes),
        width,
        height,
        &det,
        excludePerson == JNI_TRUE);
    env->ReleaseByteArrayElements(rgb, bytes, JNI_ABORT);
    if (!ok) return nullptr;

    jfloatArray arr = env->NewFloatArray(6);
    if (!arr) return nullptr;
    const float buf[6] = {det.left, det.top, det.right, det.bottom, det.score,
                          static_cast<float>(det.classId)};
    env->SetFloatArrayRegion(arr, 0, 6, buf);
    return arr;
#else
    (void)env;
    (void)rgb;
    (void)width;
    (void)height;
    (void)excludePerson;
    return nullptr;
#endif
}

JNIEXPORT jstring JNICALL
Java_com_mfhapps_trendingui_native_PretextNativeVision_nativeClassLabel(
    JNIEnv* env,
    jclass,
    jint classId) {
#ifdef PRETEXT_USE_NCNN
    return env->NewStringUTF(pretext_ncnn::classLabel(classId));
#else
    (void)classId;
    return env->NewStringUTF("object");
#endif
}

}
