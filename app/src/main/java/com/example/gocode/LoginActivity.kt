package com.example.gocode

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView


class LoginActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: android.widget.Button
    private lateinit var btnGoogle: MaterialButton
    private lateinit var btnGithub: MaterialButton
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgot: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val playerView = findViewById<PlayerView>(R.id.loginPlayerView)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val videoUri = "android.resource://$packageName/${R.raw.leo_waving}"
        val mediaItem = MediaItem.fromUri(videoUri)

        player.setMediaItem(mediaItem)
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.playWhenReady = true


        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnGithub = findViewById(R.id.btnGithub)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgot = findViewById(R.id.tvForgot)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // TODO: Firebase email/password login later
            if (email.isEmpty()) {
                etEmail.error = "Email required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password required"
                return@setOnClickListener
            }

            // For now:
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnGoogle.setOnClickListener {
            // TODO: Google sign-in later (Firebase)
        }

        btnGithub.setOnClickListener {
            // TODO: GitHub sign-in later (Firebase / OAuth)
        }

        tvForgot.setOnClickListener {
            // TODO: open Forgot Password screen
        }

        tvSignUp.setOnClickListener {
            // TODO: open Sign Up screen
        }
    }
    override fun onStop() {
        super.onStop()
        if (::player.isInitialized) {
            player.release()
        }
    }

}
