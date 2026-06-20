package com.example.gocode.lessons

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object LessonProgressStore {
    private const val PREFS_NAME = "lesson_progress"
    private const val NODE_PROGRESS = "nodeProgress"
    private const val FIELD_PROGRESS = "progressPercent"
    private const val FIELD_COMPLETED = "completed"
    private const val FIELD_UPDATED_AT = "updatedAt"
    private const val FIELD_COMPLETED_AT = "completedAt"

    fun getProgress(context: Context, nodeId: String): Int {
        return prefs(context).getInt(nodeId, 0).coerceIn(0, 100)
    }

    fun getProgressMap(context: Context, nodeIds: List<String>): Map<String, Int> {
        return nodeIds.associateWith { getProgress(context, it) }
    }

    fun getProgressMap(
        context: Context,
        nodeIds: List<String>,
        onResult: (Map<String, Int>) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(getProgressMap(context, nodeIds))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection(NODE_PROGRESS)
            .get()
            .addOnSuccessListener { snapshot ->
                val remoteProgress = snapshot.documents
                    .filter { it.id in nodeIds }
                    .associate { doc ->
                        doc.id to ((doc.getLong(FIELD_PROGRESS) ?: 0L).toInt().coerceIn(0, 100))
                    }

                if (remoteProgress.isNotEmpty()) {
                    prefs(context).edit().apply {
                        remoteProgress.forEach { (nodeId, progress) -> putInt(nodeId, progress) }
                    }.apply()
                }

                onResult(getProgressMap(context, nodeIds) + remoteProgress)
            }
            .addOnFailureListener {
                onResult(getProgressMap(context, nodeIds))
            }
    }

    fun saveProgress(context: Context, nodeId: String, progressPercent: Int) {
        val current = getProgress(context, nodeId)
        val next = progressPercent.coerceIn(0, 100)
        if (next < current) return

        prefs(context).edit()
            .putInt(nodeId, next)
            .apply()

        saveProgressToFirebase(nodeId, next)
    }

    fun clear(context: Context, onComplete: (Boolean) -> Unit = {}) {
        prefs(context).edit()
            .clear()
            .apply()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onComplete(true)
            return
        }

        val db = FirebaseFirestore.getInstance()
        val progressRef = db.collection("users")
            .document(user.uid)
            .collection(NODE_PROGRESS)

        progressRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    private fun saveProgressToFirebase(nodeId: String, progressPercent: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val progressRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection(NODE_PROGRESS)
            .document(nodeId)

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(progressRef)
            val current = (snapshot.getLong(FIELD_PROGRESS) ?: 0L).toInt().coerceIn(0, 100)
            if (progressPercent < current) return@runTransaction

            val completed = progressPercent >= 100
            val updates = mutableMapOf<String, Any>(
                FIELD_PROGRESS to progressPercent,
                FIELD_COMPLETED to completed,
                FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            )

            if (completed && snapshot.getTimestamp(FIELD_COMPLETED_AT) == null) {
                updates[FIELD_COMPLETED_AT] = FieldValue.serverTimestamp()
            }

            if (!snapshot.exists()) {
                updates["createdAt"] = FieldValue.serverTimestamp()
            }

            transaction.set(progressRef, updates, SetOptions.merge())
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
