package com.example.gocode.network

data class RunRequest(
    val language: String = "java",
    val code: String,
    val input: String? = ""
)