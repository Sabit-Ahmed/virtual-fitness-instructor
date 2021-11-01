package org.tensorflow.lite.examples.poseestimation.api.response

data class KeyPointsRestriction(
    val AngleArea: String,
    val CapturedImage: String,
    val Direction: String,
    val EndKeyPosition: String,
    val ExerciseId: Int,
    val Id: Int,
    val LineType: String,
    val MaxValidationValue: Int,
    val MiddleKeyPosition: String,
    val MinValidationValue: Int,
    val NoOfKeyPoints: Int,
    val Phase: Int,
    val Scale: String,
    val StartKeyPosition: String
)