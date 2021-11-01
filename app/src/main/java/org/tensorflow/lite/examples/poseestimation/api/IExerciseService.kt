package org.tensorflow.lite.examples.poseestimation.api

import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseListRequestPayload
import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseRequestPayload
import org.tensorflow.lite.examples.poseestimation.api.request.ExerciseTrackingPayload
import org.tensorflow.lite.examples.poseestimation.api.response.ExerciseListResponse
import org.tensorflow.lite.examples.poseestimation.api.response.ExerciseTrackingResponse
import org.tensorflow.lite.examples.poseestimation.api.response.KeyPointRestrictions
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IExerciseService {

    @POST("/api/exercisekeypoint/GetKeyPointsRestriction")
    fun getConstraint(@Body requestPayload: ExerciseRequestPayload): Call<KeyPointRestrictions>

    @Headers("Authorization: Bearer YXBpdXNlcjpZV2xoYVlUUmNHbDFjMlZ5T2lRa1RVWVRFUk1ESXc=")
    @POST("/api/exercise/SaveExerciseTracking")
    fun saveExerciseData(@Body requestPayload: ExerciseTrackingPayload): Call<ExerciseTrackingResponse>

    @Headers("Authorization: Bearer YXBpdXNlcjpZV2xoYVlUUmNHbDFjMlZ5T2lRa1RVWVRFUk1ESXc=")
    @POST("/api/exercise/getMyExercises")
    fun getExercises(@Body requestPayload: ExerciseListRequestPayload): Call<ExerciseListResponse>
}