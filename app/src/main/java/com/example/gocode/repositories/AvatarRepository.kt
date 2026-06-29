package com.example.gocode.repositories

import android.content.Context
import com.example.gocode.models.AvatarItem
import org.json.JSONArray

object AvatarRepository {
    const val DEFAULT_AVATAR_ID = "alien"

    fun load(context: Context): List<AvatarItem> {
        val json = context.assets.open("avatars.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)

        val result = mutableListOf<AvatarItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(
                AvatarItem(
                    id = obj.getString("id"),
                    drawableName = obj.getString("drawableName"),
                    displayName = obj.optString("displayName", obj.getString("id")),
                    price = obj.optLong("price", 0L),
                    rarity = obj.optString("rarity", "Common"),
                    unlockedByDefault = obj.optBoolean(
                        "unlockedByDefault",
                        obj.getString("id") == DEFAULT_AVATAR_ID
                    )
                )
            )
        }
        return result
    }

    fun defaultOwnedAvatarIds(avatars: List<AvatarItem>): Set<String> {
        return avatars.filter { it.unlockedByDefault }.map { it.id }.toSet()
            .ifEmpty { setOf(DEFAULT_AVATAR_ID) }
    }

    fun resolveDrawableResId(context: Context, drawableName: String): Int {
        return context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }
}
