package org.tensorflow.lite.examples.poseestimation.romExercise.data

import java.nio.ByteBuffer

data class MaskData(
    val height: Int,
    val width: Int,
    val buffer: ByteBuffer
)
