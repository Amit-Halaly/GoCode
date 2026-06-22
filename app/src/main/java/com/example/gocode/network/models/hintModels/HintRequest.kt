package com.example.gocode.network.models.hintModels

data class HintRequest(
    val task: String,
    val language: String,
    val code: String,
    val input: String = "",
    val output: String = "",
    val error: String = "",
    val exitCode: Int? = null,
    val passed: Boolean? = null,
    val expectedOutput: String? = null,
    val actualOutput: String? = null,
    val compareMode: String? = null
)