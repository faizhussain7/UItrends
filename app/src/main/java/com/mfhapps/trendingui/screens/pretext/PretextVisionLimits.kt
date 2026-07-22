package com.mfhapps.trendingui.screens.pretext

internal object PretextVisionLimits {
    const val MAX_FACES = 5
    const val MAX_PERSONS = 3
    const val MAX_OBJECTS = 5
    const val MAX_AUTO = 6
    const val MAX_TRACKED_SLOTS = 8

    fun maxFor(mode: VisionTrackMode): Int = when (mode) {
        VisionTrackMode.Face -> MAX_FACES
        VisionTrackMode.Person -> MAX_PERSONS
        VisionTrackMode.Object -> MAX_OBJECTS
        VisionTrackMode.Auto -> MAX_AUTO
    }
}
