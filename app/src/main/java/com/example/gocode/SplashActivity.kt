package com.example.gocode

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.VideoView
import androidx.core.net.toUri

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.splashVideo)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.spalsh_screen
        videoView.setVideoURI(videoPath.toUri())

        videoView.setOnCompletionListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        videoView.start()
    }
}
