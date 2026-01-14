package com.example.gocode.network.models.runModels

data class RunResponse(
    val output: String,
    val error: String,
    val exitCode: Int
)
