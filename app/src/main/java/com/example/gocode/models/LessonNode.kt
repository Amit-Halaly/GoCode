package com.example.gocode.models

data class LessonNode(
    val id: String,
    val title: String,
    val order: Int,
    val type: String,
    val locked: Boolean,
    val completed: Boolean
)

