package com.example.gocode.settings

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GoCodeMessagingService : FirebaseMessagingService() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid).update(
            mapOf(
                "fcmToken" to token, "notificationsEnabled" to true
            )
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

    }
}