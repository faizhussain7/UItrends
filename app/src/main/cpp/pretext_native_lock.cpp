/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#include "pretext_native_lock.h"

#include <mutex>

namespace pretext {
namespace {

std::mutex& nativeMutex() {
    static std::mutex mutex;
    return mutex;
}

}

NativeLock::NativeLock() {
    nativeMutex().lock();
}

NativeLock::~NativeLock() {
    nativeMutex().unlock();
}

}
