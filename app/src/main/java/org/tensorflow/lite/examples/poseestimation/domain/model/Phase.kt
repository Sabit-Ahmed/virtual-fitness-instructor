package org.tensorflow.lite.examples.poseestimation.domain.model

data class Phase(
    val phase: Int,
    val constraints: List<Constraint>
)