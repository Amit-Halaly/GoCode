package com.example.gocode.network.models.hintModels

data class HintRequest(
    val task: String,
    val language: String,
    val code: String,
    val input: String = "",
    val output: String = "",
    val error: String = "",
    val exitCode: Int? = null
)