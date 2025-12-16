package com.example.gocode

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var placeholder: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        videoView = findViewById(R.id.splashVideo)
        placeholder = findViewById(R.id.splashPlaceholder)

        placeholder.alpha = 1f
        placeholder.visibility = View.VISIBLE

        val videoPath = "android.resource://$packageName/${R.raw.spalsh_screen}"
        videoView.setVideoURI(videoPath.toUri())

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false

            mp.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    placeholder.animate()
                        .alpha(0f)
                        .setDuration(120)
                        .withEndAction { placeholder.visibility = View.GONE }
                        .start()
                    true
                } else {
                    false
                }
            }
        }

        videoView.setOnCompletionListener {
            navigateNext()
        }

        videoView.start()
    }

    private fun navigateNext() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        user.reload()
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}
