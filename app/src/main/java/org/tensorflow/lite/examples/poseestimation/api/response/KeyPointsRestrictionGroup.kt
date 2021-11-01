package org.tensorflow.lite.examples.poseestimation.api.response

data class KeyPointsRestrictionGroup(
    val KeyPointsRestriction: List<KeyPointsRestriction>,
    val Phase: Int
)