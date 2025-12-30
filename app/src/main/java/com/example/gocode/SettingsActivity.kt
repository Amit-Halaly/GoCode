package com.example.gocode

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SettingsActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var userListener: ListenerRegistration? = null

    private lateinit var prefs: SharedPreferences
    private lateinit var avatarIv: ImageView

    private val avatarPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            val avatarId =
                result.data?.getStringExtra("selectedAvatarId") ?: return@registerForActivityResult
            updateAvatarUI(avatarId)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        avatarIv = findViewById(R.id.settingsAvatar)

        setupBackButton()
        setupTitles()
        setupSwitches()
        setupNavigationItems()
        attachAvatarListener()
    }


    private fun setupBackButton() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }


    private fun setupSwitches() {

        val notificationsSwitch =
            findViewById<View>(R.id.itemNotifications).findViewById<SwitchMaterial>(R.id.switchItem)

        val learningModeSwitch =
            findViewById<View>(R.id.itemLearningMode).findViewById<SwitchMaterial>(R.id.switchItem)

        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)

        learningModeSwitch.isChecked = prefs.getBoolean("learning_mode", false)

        notificationsSwitch.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("notifications_enabled", v).apply()
        }

        learningModeSwitch.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("learning_mode", v).apply()
        }
    }


    private fun setupTitles() {
        findViewById<View>(R.id.itemEditProfile).findViewById<TextView>(R.id.title).text =
            "Edit Profile"

        findViewById<View>(R.id.itemChangeAvatar).findViewById<TextView>(R.id.title).text =
            "Change Avatar"

        findViewById<View>(R.id.itemNotifications).findViewById<TextView>(R.id.title).text =
            "Notifications"

        findViewById<View>(R.id.itemLearningMode).findViewById<TextView>(R.id.title).text =
            "Learning Mode"

        findViewById<View>(R.id.itemAbout).findViewById<TextView>(R.id.title).text = "About"
    }


    private fun setupNavigationItems() {
        findViewById<View>(R.id.itemChangeAvatar).setOnClickListener {
            avatarPickerLauncher.launch(
                Intent(this, AvatarPickerActivity::class.java)
            )
        }

        findViewById<View>(R.id.itemAbout).setOnClickListener {
            startActivity(
                Intent(this, AboutActivity::class.java)
            )
        }

    }


    private fun attachAvatarListener() {
        val user = auth.currentUser ?: return

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, _ ->
            val avatarId = doc?.getString("avatarId") ?: return@addSnapshotListener
            updateAvatarUI(avatarId)
        }
    }


    private fun updateAvatarUI(avatarId: String) {
        val avatars = AvatarRepository.load(this)
        val avatar = avatars.firstOrNull { it.id == avatarId } ?: return

        val resId = AvatarRepository.resolveDrawableResId(this, avatar.drawableName)
        if (resId != 0) avatarIv.setImageResource(resId)
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
        userListener = null
    }
}
