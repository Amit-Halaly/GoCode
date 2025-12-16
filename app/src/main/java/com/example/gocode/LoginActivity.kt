package com.example.gocode

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var player: ExoPlayer? = null

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: android.widget.Button
    private lateinit var btnGoogle: MaterialButton
    private lateinit var btnGithub: MaterialButton
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgot: TextView

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: throw Exception("Missing ID Token")

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { goMain() }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Google login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } catch (e: Exception) {
                Toast.makeText(this, "Google login cancelled/failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val playerView = findViewById<PlayerView>(R.id.loginPlayerView)
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            val videoUri = "android.resource://$packageName/${R.raw.leo_waving}"
            exo.setMediaItem(MediaItem.fromUri(videoUri))
            exo.repeatMode = Player.REPEAT_MODE_ONE
            exo.prepare()
            exo.playWhenReady = true
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnGithub = findViewById(R.id.btnGithub)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgot = findViewById(R.id.tvForgot)

        btnLogin.setOnClickListener { loginWithEmail() }
        btnGoogle.setOnClickListener { loginWithGoogle() }
        btnGithub.setOnClickListener { loginWithGithub() }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Forgot password - next step", Toast.LENGTH_SHORT).show()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginWithEmail() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty()) { etEmail.error = "Email required"; return }
        if (password.isEmpty()) { etPassword.error = "Password required"; return }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { goMain() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loginWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)

        client.signOut().addOnCompleteListener {
            googleLauncher.launch(client.signInIntent)
        }
    }

    private fun loginWithGithub() {
        val provider = OAuthProvider.newBuilder("github.com")
            .addCustomParameter("allow_signup", "true")
            .build()

        auth.startActivityForSignInWithProvider(this, provider)
            .addOnSuccessListener { goMain() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "GitHub login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

}
