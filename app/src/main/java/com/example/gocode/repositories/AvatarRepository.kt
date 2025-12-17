package com.example.gocode.repositories

import android.content.Context
import com.example.gocode.models.AvatarItem
import org.json.JSONArray

object AvatarRepository {

    fun load(context: Context): List<AvatarItem> {
        val json = context.assets.open("avatars.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)

        val result = mutableListOf<AvatarItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(
                AvatarItem(
                    id = obj.getString("id"),
                    drawableName = obj.getString("drawableName")
                )
            )
        }
        return result
    }

    fun resolveDrawableResId(context: Context, drawableName: String): Int {
        return context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }
}