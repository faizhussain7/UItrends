package com.mfhapps.trendingui.util

import android.annotation.SuppressLint
import android.os.Trace

@SuppressLint("UnclosedTrace")
inline fun <T> traceSection(section: String, block: () -> T): T {
    Trace.beginSection(section)
    try {
        return block()
    } finally {
        Trace.endSection()
    }
}
