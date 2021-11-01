package org.tensorflow.lite.examples.poseestimation.domain.model

import org.tensorflow.lite.examples.poseestimation.exercise.IExercise

data class TestId(
    val id: String,
    val exercises: List<IExercise>
)
