package com.example.gocode

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.gocode.repositories.AvatarRepository


class SettingsActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var userListener: ListenerRegistration? = null

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // init prefs
        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        setupBackButton()
        setupTitles()
        setupSwitches()
        setupAvatarListener()

        // TODO: לחבר ניווט למסכים:
        // setupNavigationItems()
    }

    // ─────────────────────────────
    // Back button
    // ─────────────────────────────
    private fun setupBackButton() {
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    // ─────────────────────────────
    // Switches logic
    // ─────────────────────────────
    private fun setupSwitches() {

        // Notifications switch
        val notificationsSwitch =
            findViewById<android.view.View>(R.id.itemNotifications).findViewById<SwitchMaterial>(R.id.switchItem)

        // Learning mode switch
        val learningModeSwitch =
            findViewById<android.view.View>(R.id.itemLearningMode).findViewById<SwitchMaterial>(R.id.switchItem)

        // Load saved values
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)

        learningModeSwitch.isChecked = prefs.getBoolean("learning_mode", false)

        // Save on change
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        learningModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("learning_mode", isChecked).apply()
        }
    }

    /*
    ─────────────────────────────
    TODO – Navigation items (בהמשך)
    ─────────────────────────────

    private fun setupNavigationItems() {

        findViewById<View>(R.id.itemEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<View>(R.id.itemChangeAvatar).setOnClickListener {
            startActivity(Intent(this, AvatarPickerActivity::class.java))
        }

        findViewById<View>(R.id.itemAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
    */

    private fun setupTitles() {

        // Edit Profile
        findViewById<View>(R.id.itemEditProfile).findViewById<TextView>(R.id.title).text =
            "Edit Profile"

        // Change Avatar
        findViewById<View>(R.id.itemChangeAvatar).findViewById<TextView>(R.id.title).text =
            "Change Avatar"

        // Notifications
        findViewById<View>(R.id.itemNotifications).findViewById<TextView>(R.id.title).text =
            "Notifications"

        // Learning Mode
        findViewById<View>(R.id.itemLearningMode).findViewById<TextView>(R.id.title).text =
            "Learning Mode"

        // About
        findViewById<View>(R.id.itemAbout).findViewById<TextView>(R.id.title).text = "About"
    }

    private fun setupAvatarListener() {
        val user = auth.currentUser ?: return

        val avatarIv = findViewById<ImageView>(R.id.settingsAvatar)

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            doc.getString("avatarId")?.takeIf { it.isNotBlank() }?.let { avatarId ->

                val avatars = AvatarRepository.load(this)
                val avatarItem = avatars.firstOrNull { it.id == avatarId }

                if (avatarItem != null) {
                    val resId = AvatarRepository.resolveDrawableResId(
                        this, avatarItem.drawableName
                    )
                    if (resId != 0) {
                        avatarIv.setImageResource(resId)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
        userListener = null
    }


}
