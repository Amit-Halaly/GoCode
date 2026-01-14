package com.example.gocode.lessons

data class LessonStep(
    val id: String,
    val title: String,
    val body: String,
    val code: String? = null,
    val tip: String? = null
)
