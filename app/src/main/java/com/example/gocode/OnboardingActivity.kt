package com.example.gocode

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.example.gocode.adapters.AvatarAdapter
import com.example.gocode.repositories.AvatarRepository

class OnboardingActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var etUsername: EditText

    private lateinit var btnPython: AppCompatButton
    private lateinit var btnJava: AppCompatButton
    private lateinit var btnC: AppCompatButton

    private lateinit var btnBeginner: AppCompatButton
    private lateinit var btnIntermediate: AppCompatButton
    private lateinit var btnAdvanced: AppCompatButton

    private lateinit var rvAvatars: RecyclerView
    private lateinit var btnContinue: AppCompatButton

    private var selectedLanguage = "Python"
    private var selectedSkill = "Beginner"
    private var selectedAvatarId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        etUsername = findViewById(R.id.etUsername)

        btnPython = findViewById(R.id.btnPython)
        btnJava = findViewById(R.id.btnJava)
        btnC = findViewById(R.id.btnC)

        btnBeginner = findViewById(R.id.btnBeginner)
        btnIntermediate = findViewById(R.id.btnIntermediate)
        btnAdvanced = findViewById(R.id.btnAdvanced)

        rvAvatars = findViewById(R.id.rvAvatars)
        btnContinue = findViewById(R.id.btnContinue)

        selectLanguage("Python")
        selectSkill("Beginner")

        btnPython.setOnClickListener { selectLanguage("Python") }
        btnJava.setOnClickListener { selectLanguage("Java") }
        btnC.setOnClickListener { selectLanguage("C") }

        btnBeginner.setOnClickListener { selectSkill("Beginner") }
        btnIntermediate.setOnClickListener { selectSkill("Intermediate") }
        btnAdvanced.setOnClickListener { selectSkill("Advanced") }

        setupAvatarsFromAssets()

        btnContinue.setOnClickListener { continueNext() }
    }

    private fun setupAvatarsFromAssets() {
        val avatars = AvatarRepository.load(this)
        selectedAvatarId = avatars.firstOrNull()?.id ?: ""

        rvAvatars.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvAvatars.adapter = AvatarAdapter(avatars, selectedAvatarId) { chosen ->
            selectedAvatarId = chosen.id
        }
    }

    private fun selectLanguage(lang: String) {
        selectedLanguage = lang
        btnPython.setBackgroundResource(if (lang == "Python") R.drawable.bg_choice_selected else R.drawable.bg_choice_unselected)
        btnJava.setBackgroundResource(if (lang == "Java") R.drawable.bg_choice_selected else R.drawable.bg_choice_unselected)
        btnC.setBackgroundResource(if (lang == "C") R.drawable.bg_choice_selected else R.drawable.bg_choice_unselected)
    }

    private fun selectSkill(skill: String) {
        selectedSkill = skill
        btnBeginner.setBackgroundResource(if (skill == "Beginner") R.drawable.bg_choice_selected else R.drawable.bg_choice_unselected)
        btnIntermediate.setBackgroundResource(if (skill == "Intermediate") R.drawable.bg_choice_selected else R.drawable.bg_choice_unselected)
        btnAdvanced.setBackgroundResource(if (skill == "Advanced") R.drawable.bg_choice_selected else R.drawable.bg_choice_unselected)
    }

    private fun continueNext() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val username = etUsername.text.toString().trim()
        if (username.isEmpty()) {
            etUsername.error = "Username required"
            return
        }
        if (selectedAvatarId.isBlank()) {
            Toast.makeText(this, "Choose an avatar", Toast.LENGTH_SHORT).show()
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
