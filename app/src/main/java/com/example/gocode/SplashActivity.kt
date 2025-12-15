package com.example.gocode

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.VideoView
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.splashVideo)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.spalsh_screen
        videoView.setVideoURI(videoPath.toUri())

        videoView.setOnCompletionListener {
            navigateNext()
        }

        videoView.start()
    }

    private fun navigateNext() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
