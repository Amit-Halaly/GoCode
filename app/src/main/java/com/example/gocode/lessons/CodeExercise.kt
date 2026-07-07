package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

data class CodeExercise(
    val nodeId: String,
    val language: String = "java",
    val title: String,
    val subtitle: String,
    val template: String,
    val answer: String,
    val defaultInput: String = "",
    val compareMode: String = "trim",
    val tests: List<RunTestCase>
)
