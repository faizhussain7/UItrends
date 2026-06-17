/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 MFH Apps
 */

#pragma once

namespace pretext {

class NativeLock {
public:
    NativeLock();
    ~NativeLock();
    NativeLock(const NativeLock&) = delete;
    NativeLock& operator=(const NativeLock&) = delete;
};

}
