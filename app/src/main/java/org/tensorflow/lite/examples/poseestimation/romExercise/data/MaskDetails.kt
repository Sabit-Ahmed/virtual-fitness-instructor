package org.tensorflow.lite.examples.poseestimation.romExercise.data

data class MaskDetails(
    val totalConfidence: Float,
    val pixelDifferenceX: Float,
    val pixelDifferenceY: Float,
    val topPoint: Point,
    val bottomPoint: Point,
    val leftPoint: Point,
    val rightPoint: Point
)
