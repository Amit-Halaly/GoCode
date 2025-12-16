package com.example.gocode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var btnCreate: Button
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        playerView = findViewById(R.id.signupPlayerView)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirm = findViewById(R.id.etConfirmPassword)
        btnCreate = findViewById(R.id.btnCreateAccount)
        tvBack = findViewById(R.id.tvBackToLogin)

        setupFrozenTopVideo(R.raw.signup_success)

        btnCreate.setOnClickListener { createAccount() }
        tvBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupFrozenTopVideo(rawRes: Int) {
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo

            val uri = "android.resource://$packageName/$rawRes"
            exo.setMediaItem(MediaItem.fromUri(uri))

            exo.repeatMode = Player.REPEAT_MODE_OFF
            exo.playWhenReady = false
            exo.prepare()
        }
    }

    private fun playSuccessVideoThenGoHome() {
        val exo = player ?: return

        exo.seekTo(0)
        exo.playWhenReady = true

        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    exo.removeListener(this)
                    goHome()
                }
            }
        })
    }

    private fun createAccount() {
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString()
        val confirm = etConfirm.text.toString()

        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return
        }

        val passwordError = isPasswordValid(pass)
        if (passwordError != null) {
            etPassword.error = passwordError
            return
        }

        if (pass != confirm) {
            etConfirm.error = "Passwords do not match"
            return
        }

        btnCreate.isEnabled = false

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                playSuccessVideoThenGoHome()
            }
            .addOnFailureListener { e ->
                btnCreate.isEnabled = true
                Toast.makeText(this, "Sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isPasswordValid(password: String): String? {
        if (password.length < 8)
            return "Password must be at least 8 characters"

        if (!password.any { it.isUpperCase() })
            return "Password must contain at least one uppercase letter"

        if (!password.any { it.isLowerCase() })
            return "Password must contain at least one lowercase letter"

        if (!password.any { it.isDigit() })
            return "Password must contain at least one number"

        if (!password.any { "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/".contains(it) })
            return "Password must contain at least one special character"

        if (password.contains(" "))
            return "Password must not contain spaces"

        return null
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
