package com.example.gocode.settings

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.gocode.R
import com.example.gocode.repositories.AvatarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class EditProfileActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var userListener: ListenerRegistration? = null

    private lateinit var etUsername: EditText
    private lateinit var avatarImage: ImageView

    private lateinit var btnPython: AppCompatButton
    private lateinit var btnJava: AppCompatButton
    private lateinit var btnC: AppCompatButton
    private lateinit var btnSave: AppCompatButton

    private var selectedLanguage: String = "Python"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        bindViews()
        setupLanguageButtons()
        listenToUser()
        setupSave()
    }

    private fun bindViews() {
        etUsername = findViewById(R.id.etUsername)
        avatarImage = findViewById(R.id.avatarImage)

        btnPython = findViewById(R.id.btnPython)
        btnJava = findViewById(R.id.btnJava)
        btnC = findViewById(R.id.btnC)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun listenToUser() {
        val user = auth.currentUser ?: return

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            etUsername.setText(doc.getString("username") ?: "")

            selectedLanguage = doc.getString("primaryLanguage") ?: "Python"
            updateLanguageUI()

            doc.getString("avatarId")?.let { avatarId ->
                val avatar = AvatarRepository.load(this).firstOrNull { it.id == avatarId }

                avatar?.let {
                    val resId = AvatarRepository.resolveDrawableResId(
                        this, it.drawableName
                    )
                    if (resId != 0) {
                        avatarImage.setImageResource(resId)
                    }
                }
            }
        }
    }

    private fun setupLanguageButtons() {

        btnPython.setOnClickListener {
            selectedLanguage = "Python"
            updateLanguageUI()
        }

        btnJava.setOnClickListener {
            selectedLanguage = "Java"
            updateLanguageUI()
        }

        btnC.setOnClickListener {
            selectedLanguage = "C"
            updateLanguageUI()
        }
    }

    private fun updateLanguageUI() {
        btnPython.setBackgroundResource(
            if (selectedLanguage == "Python") R.drawable.bg_choice_selected
            else R.drawable.bg_choice_unselected
        )
        btnJava.setBackgroundResource(
            if (selectedLanguage == "Java") R.drawable.bg_choice_selected
            else R.drawable.bg_choice_unselected
        )
        btnC.setBackgroundResource(
            if (selectedLanguage == "C") R.drawable.bg_choice_selected
            else R.drawable.bg_choice_unselected
        )
    }

    private fun setupSave() {
        btnSave.setOnClickListener {
            val user = auth.currentUser ?: return@setOnClickListener

            val username = etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "username" to username, "primaryLanguage" to selectedLanguage
            )

            db.collection("users").document(user.uid).update(updates).addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
        userListener = null
    }
}