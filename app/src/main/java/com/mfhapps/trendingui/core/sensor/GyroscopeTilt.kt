package com.mfhapps.trendingui.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

data class GyroTilt(val pitch: Float, val roll: Float, val azimuth: Float)

@Composable
fun rememberGyroscopeTilt(sensitivity: Float = 0.3f): GyroTilt {
    val context = LocalContext.current
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var azimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context, sensitivity) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = orientation[0] * sensitivity
                pitch = orientation[1] * sensitivity
                roll = orientation[2] * sensitivity
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return GyroTilt(pitch = pitch, roll = roll, azimuth = azimuth)
}
