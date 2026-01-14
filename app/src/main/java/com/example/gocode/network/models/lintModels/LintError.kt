package com.example.gocode.network.models.lintModels

data class LintError(
    val line: Int,
    val col: Int? = null,
    val message: String
)