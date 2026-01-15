package com.example.gocode.network

import com.example.gocode.network.models.lintModels.LintRequest
import com.example.gocode.network.models.lintModels.LintResponse
import com.example.gocode.network.models.runModels.RunRequest
import com.example.gocode.network.models.runModels.RunResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ExecApi {
    @POST("run")
    suspend fun run(@Body req: RunRequest): RunResponse

    @POST("/lint")
    suspend fun lint(@Body req: LintRequest): LintResponse

}
