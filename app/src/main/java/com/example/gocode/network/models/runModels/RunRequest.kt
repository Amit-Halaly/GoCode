package com.example.gocode.network.models.runModels

data class RunRequest(
    val language: String = "java",
    val code: String,
    val input: String? = ""
)