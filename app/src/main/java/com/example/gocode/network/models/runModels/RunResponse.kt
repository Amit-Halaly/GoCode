package com.example.gocode.network.models.runModels

data class RunResponse(
    val output: String,
    val error: String,
    val exitCode: Int,
    val passed: Boolean? = null,
    val expectedOutput: String? = null,
    val actualOutput: String? = null
)

