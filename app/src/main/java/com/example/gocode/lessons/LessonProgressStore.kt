package com.example.gocode.lessons

import android.content.Context

object LessonProgressStore {
    private const val PREFS_NAME = "lesson_progress"

    fun getProgress(context: Context, nodeId: String): Int {
        return prefs(context).getInt(nodeId, 0).coerceIn(0, 100)
    }

    fun getProgressMap(context: Context, nodeIds: List<String>): Map<String, Int> {
        return nodeIds.associateWith { getProgress(context, it) }
    }

    fun saveProgress(context: Context, nodeId: String, progressPercent: Int) {
        val current = getProgress(context, nodeId)
        val next = progressPercent.coerceIn(0, 100)
        if (next < current) return

        prefs(context).edit()
            .putInt(nodeId, next)
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
