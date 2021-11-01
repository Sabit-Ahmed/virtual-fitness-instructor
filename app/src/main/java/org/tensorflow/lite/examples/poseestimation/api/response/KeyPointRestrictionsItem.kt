package org.tensorflow.lite.examples.poseestimation.api.response

data class KeyPointRestrictionsItem(
    val ExerciseId: Int,
    val KeyPointsRestrictionGroup: List<KeyPointsRestrictionGroup>,
    val Tenant: String
)