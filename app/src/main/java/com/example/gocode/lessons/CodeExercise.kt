package com.example.gocode.lessons

import com.example.gocode.network.models.runModels.RunTestCase

data class CodeExercise(
    val nodeId: String,
    val title: String,
    val subtitle: String,
    val template: String,
    val defaultInput: String = "",
    val compareMode: String = "trim",
    val tests: List<RunTestCase>
)
