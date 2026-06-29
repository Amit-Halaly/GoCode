package com.example.gocode.models

data class AvatarItem(
    val id: String,
    val drawableName: String,
    val displayName: String,
    val price: Long,
    val rarity: String,
    val unlockedByDefault: Boolean
)
