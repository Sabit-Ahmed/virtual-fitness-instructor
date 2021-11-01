package org.tensorflow.lite.examples.poseestimation.api.request

data class LogInRequest(
    val Email: String,
    val Password: String,
    val Tenant: String
)