package org.tensorflow.lite.examples.poseestimation.domain.model

import android.graphics.Color

data class Constraint(
    val type: ConstraintType,
    val startPointIndex: Int,
    val middlePointIndex: Int,
    val endPointIndex: Int,
    val clockWise: Boolean = false,
    val color: Int = Color.WHITE,
    val minValue: Int,
    val maxValue: Int
)