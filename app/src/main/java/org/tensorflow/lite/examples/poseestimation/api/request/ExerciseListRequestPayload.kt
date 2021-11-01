package org.tensorflow.lite.examples.poseestimation.api.request

data class ExerciseListRequestPayload(
    val PatientId: String,
    val start: Int,
    val pageSize: Int,
    val Tenant: String
)