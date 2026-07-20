package com.mfhapps.trendingui.screens.pretext

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import kotlin.math.min
import kotlin.math.roundToInt

enum class PretextRecordingQualityMode {
    Auto,
    High,
    Balanced,
    BatterySaver,
    ;

    val label: String
        get() = when (this) {
            Auto -> "Auto"
            High -> "High"
            Balanced -> "Balanced"
            BatterySaver -> "Battery saver"
        }

    val description: String
        get() = when (this) {
            Auto -> "Adapts frame rate, resolution, and bitrate to this device."
            High -> "Best quality — up to 60 fps and 1080p when the device can keep up."
            Balanced -> "Smooth 30 fps at 1080p for most phones."
            BatterySaver -> "24 fps at 720p to reduce heat and storage use."
        }
}

data class PretextRecordingProfile(
    val targetFps: Int,
    val maxLongEdgePx: Int,
    val videoBitrate: Int,
    val qualityMode: PretextRecordingQualityMode,
    val deviceAdaptive: Boolean,
) {
    val resolutionLabel: String
        get() = when {
            maxLongEdgePx >= 1920 -> "1080p+"
            maxLongEdgePx >= 1080 -> "1080p"
            else -> "720p"
        }

    fun summaryLabel(): String = buildString {
        append(targetFps)
        append(" fps · ")
        append(resolutionLabel)
        append(" · ")
        append(formatBitrate(videoBitrate))
        if (deviceAdaptive) {
            append(" · Auto")
        }
    }

    companion object {
        fun recommendBitrate(width: Int, height: Int, fps: Int): Int {
            val pixels = width.toLong() * height.toLong()
            val raw = (pixels * fps * 0.12).toInt()
            return raw.coerceIn(5_000_000, 24_000_000)
        }

        private fun formatBitrate(bitsPerSecond: Int): String {
            val mbps = bitsPerSecond / 1_000_000f
            return if (mbps >= 10f) {
                "${mbps.roundToInt()} Mbps"
            } else {
                "%.1f Mbps".format(mbps)
            }
        }
    }
}

internal object PretextRecordingProfileResolver {
    fun resolve(context: Context, qualityMode: PretextRecordingQualityMode): PretextRecordingProfile {
        val device = readDeviceCapabilities(context)
        return when (qualityMode) {
            PretextRecordingQualityMode.Auto -> resolveAuto(device)
            PretextRecordingQualityMode.High -> resolvePreset(
                qualityMode = qualityMode,
                targetFps = min(60, device.maxRefreshRate.roundToInt()).coerceAtLeast(30),
                maxLongEdgePx = if (device.lowRamDevice) 1080 else 1920,
                videoBitrate = 18_000_000,
                deviceAdaptive = false,
            )
            PretextRecordingQualityMode.Balanced -> resolvePreset(
                qualityMode = qualityMode,
                targetFps = min(30, device.maxRefreshRate.roundToInt()).coerceAtLeast(24),
                maxLongEdgePx = if (device.lowRamDevice) 720 else 1080,
                videoBitrate = 10_000_000,
                deviceAdaptive = false,
            )
            PretextRecordingQualityMode.BatterySaver -> resolvePreset(
                qualityMode = qualityMode,
                targetFps = 24,
                maxLongEdgePx = 720,
                videoBitrate = 4_000_000,
                deviceAdaptive = false,
            )
        }
    }

    fun deviceSummary(context: Context): String {
        val device = readDeviceCapabilities(context)
        return buildString {
            append(device.maxRefreshRate.roundToInt())
            append(" Hz display · ")
            append(device.processorCount)
            append(" cores · ")
            append(device.memoryClassMb)
            append(" MB heap")
            if (device.lowRamDevice) {
                append(" · low RAM")
            }
        }
    }

    private data class DeviceRecordingCapabilities(
        val maxRefreshRate: Float,
        val memoryClassMb: Int,
        val processorCount: Int,
        val lowRamDevice: Boolean,
    )

    private fun readDeviceCapabilities(context: Context): DeviceRecordingCapabilities {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val refreshRate = readMaxRefreshRate(context)

        return DeviceRecordingCapabilities(
            maxRefreshRate = refreshRate.coerceIn(24f, 120f),
            memoryClassMb = activityManager?.memoryClass ?: 128,
            processorCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(4),
            lowRamDevice = activityManager?.isLowRamDevice == true,
        )
    }

    private fun readMaxRefreshRate(context: Context): Float {
        val display = context.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
            ?: return 60f

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            display.supportedModes.maxOfOrNull { it.refreshRate } ?: display.refreshRate
        } else {
            @Suppress("DEPRECATION")
            display.refreshRate
        }
    }

    private fun resolveAuto(device: DeviceRecordingCapabilities): PretextRecordingProfile {
        val maxLongEdgePx = when {
            device.lowRamDevice || device.memoryClassMb < 96 -> 720
            device.memoryClassMb >= 256 && device.processorCount >= 6 -> 1920
            device.memoryClassMb >= 128 -> 1080
            else -> 720
        }

        val targetFps = when {
            device.lowRamDevice -> 24
            device.maxRefreshRate >= 90f &&
                device.memoryClassMb >= 256 &&
                device.processorCount >= 8 -> min(60, device.maxRefreshRate.roundToInt())
            device.maxRefreshRate >= 60f &&
                device.memoryClassMb >= 128 &&
                device.processorCount >= 4 -> min(30, device.maxRefreshRate.roundToInt())
            else -> 24
        }.coerceIn(24, 60)

        val referenceBitrate = when {
            maxLongEdgePx >= 1920 && targetFps >= 60 -> 20_000_000
            maxLongEdgePx >= 1920 -> 16_000_000
            maxLongEdgePx >= 1080 && targetFps >= 30 -> 12_000_000
            maxLongEdgePx >= 1080 -> 10_000_000
            targetFps >= 30 -> 6_000_000
            else -> 5_000_000
        }

        return PretextRecordingProfile(
            targetFps = targetFps,
            maxLongEdgePx = maxLongEdgePx,
            videoBitrate = referenceBitrate,
            qualityMode = PretextRecordingQualityMode.Auto,
            deviceAdaptive = true,
        )
    }

    private fun resolvePreset(
        qualityMode: PretextRecordingQualityMode,
        targetFps: Int,
        maxLongEdgePx: Int,
        videoBitrate: Int,
        deviceAdaptive: Boolean,
    ): PretextRecordingProfile {
        val evenLongEdge = maxLongEdgePx.coerceAtLeast(720)
        val referenceWidth = if (evenLongEdge >= 1920) 1080 else 720
        val referenceHeight = if (evenLongEdge >= 1920) 1920 else 1280
        val tunedBitrate = maxOf(
            videoBitrate,
            PretextRecordingProfile.recommendBitrate(referenceWidth, referenceHeight, targetFps),
        )
        return PretextRecordingProfile(
            targetFps = targetFps.coerceIn(24, 60),
            maxLongEdgePx = evenLongEdge,
            videoBitrate = tunedBitrate,
            qualityMode = qualityMode,
            deviceAdaptive = deviceAdaptive,
        )
    }
}
