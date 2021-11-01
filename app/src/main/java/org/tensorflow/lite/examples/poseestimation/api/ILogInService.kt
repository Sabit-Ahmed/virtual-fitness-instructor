package org.tensorflow.lite.examples.poseestimation.api

import org.tensorflow.lite.examples.poseestimation.api.request.LogInRequest
import org.tensorflow.lite.examples.poseestimation.api.response.LogInResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ILogInService {

    @POST("/api/Account/GetCrmContact")
    fun logIn(@Body requestPayload: LogInRequest): Call<LogInResponse>
}