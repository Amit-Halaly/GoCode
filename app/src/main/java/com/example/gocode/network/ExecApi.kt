package com.example.gocode.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ExecApi {
    @POST("run")
    suspend fun run(@Body req: RunRequest): RunResponse
}
