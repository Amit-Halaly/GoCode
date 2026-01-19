package com.example.gocode.lessons

data class PathNodeItem(
    val id: String,
    val type: PathNodeType,
    val title: String,
    val offsetDp: Int = 0,
    val progressPercent: Int = 0,
    val locked: Boolean = true
)
