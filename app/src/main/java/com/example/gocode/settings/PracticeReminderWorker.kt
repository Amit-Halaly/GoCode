package com.example.gocode.settings

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gocode.R
import kotlin.random.Random

class PracticeReminderWorker(
    private val context: Context, params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        createChannel()

        val messages = listOf(
            "wallac Time to practice 🚀",
            "wallac One challenge a day keeps the bugs away 🧠",
            "wallac Your streak is waiting 🔥",
            "wallac Sharpen your coding skills today 💻",
            "wallac 5 minutes of practice goes a long way ⏱️",
            "wallac i miss you pls come back to me"
        )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GoCode").setContentText(messages.random())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)

        return Result.success()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "GoCode Practice Reminders", NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "gocode_daily_reminder"
    }
}
