package com.example.gocode

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gocode.firebase.FirebaseContentRepository
import com.example.gocode.friends.FriendRequest
import com.example.gocode.friends.FriendsRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private var friendRequestsListener: ListenerRegistration? = null
    private var knownPendingFriendRequestIds: Set<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseContentRepository.ensureSeeded(this)
        FriendsRepository.syncCurrentUserSearchFields()
        listenForFriendRequestNotifications()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setupWithNavController(
            navController
        )


    }

    private fun listenForFriendRequestNotifications() {
        friendRequestsListener = FriendsRepository.listenIncomingRequests(
            onChanged = { requests ->
                runOnUiThread {
                    handleFriendRequestNotification(requests)
                }
            },
            onError = {
                knownPendingFriendRequestIds = emptySet()
            },
        )
    }

    private fun handleFriendRequestNotification(requests: List<FriendRequest>) {
        val currentIds = requests.map { it.id }.toSet()
        val previousIds = knownPendingFriendRequestIds
        knownPendingFriendRequestIds = currentIds
        if (previousIds == null) return

        val newRequest = requests.firstOrNull { it.id !in previousIds } ?: return
        showFriendRequestNotification(newRequest)
    }

    private fun showFriendRequestNotification(request: FriendRequest) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        createFriendRequestChannel()
        val notification = NotificationCompat.Builder(this, FRIEND_REQUEST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("New friend request")
            .setContentText("${request.fromUsername} wants to be friends")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(request.id.hashCode(), notification)
    }

    private fun createFriendRequestChannel() {
        val channel = NotificationChannel(
            FRIEND_REQUEST_CHANNEL_ID,
            "GoCode Friend Requests",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        friendRequestsListener?.remove()
        friendRequestsListener = null
    }

    companion object {
        private const val FRIEND_REQUEST_CHANNEL_ID = "gocode_friend_requests"
    }
}


