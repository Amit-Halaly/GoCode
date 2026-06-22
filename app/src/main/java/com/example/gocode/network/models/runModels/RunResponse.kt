package com.example.gocode.network.models.runModels

data class RunResponse(
    val output: String,
    val error: String,
    val exitCode: Int,
    val passed: Boolean? = null,
    val expectedOutput: String? = null,
    val actualOutput: String? = null,
    val testResults: List<RunTestResult>? = null,
    val summary: String? = null
)

data class RunTestResult(
    val name: String? = null,
    val passed: Boolean,
    val input: String? = "",
    val expectedOutput: String? = null,
    val actualOutput: String? = null,
    val error: String = "",
    val exitCode: Int = 0,
    val hidden: Boolean = false
)
