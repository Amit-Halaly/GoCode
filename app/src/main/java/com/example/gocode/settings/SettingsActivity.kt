package com.example.gocode.settings

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gocode.R
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class SettingsActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var userListener: ListenerRegistration? = null

    private lateinit var prefs: SharedPreferences
    private lateinit var avatarIv: ImageView

    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchAndSaveFcmToken()
                setNotificationsEnabled(true)
            } else {
                setNotificationsEnabled(false)
                updateNotificationsSwitchUi(false)
            }
        }

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

        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", false)

        learningModeSwitch.isChecked = prefs.getBoolean("learning_mode", false)

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableNotificationsFlow()
            } else {
                setNotificationsEnabled(false)
            }
        }

        learningModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("learning_mode", isChecked).apply()
        }
    }

    private fun enableNotificationsFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                fetchAndSaveFcmToken()
                setNotificationsEnabled(true)
            } else {
                requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            fetchAndSaveFcmToken()
            setNotificationsEnabled(true)
        }
    }

    private fun fetchAndSaveFcmToken() {
        val user = auth.currentUser ?: return

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("users").document(user.uid).set(
                mapOf("fcmToken" to token), SetOptions.merge()
            )
        }
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()

        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).set(
            mapOf("notificationsEnabled" to enabled), SetOptions.merge()
        )
    }

    private fun updateNotificationsSwitchUi(checked: Boolean) {
        val notificationsSwitch =
            findViewById<View>(R.id.itemNotifications).findViewById<SwitchMaterial>(R.id.switchItem)

        notificationsSwitch.setOnCheckedChangeListener(null)
        notificationsSwitch.isChecked = checked
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) enableNotificationsFlow()
            else setNotificationsEnabled(false)
        }
    }

    private fun setupTitles() {
        findViewById<View>(R.id.itemEditProfile).findViewById<TextView>(R.id.title).text =
            "Edit Profile"
        findViewById<View>(R.id.itemChangeAvatar).findViewById<TextView>(R.id.title).text =
            "Change Avatar"
        findViewById<View>(R.id.itemNotifications).findViewById<TextView>(R.id.title).text =
            "settings"
        findViewById<View>(R.id.itemLearningMode).findViewById<TextView>(R.id.title).text =
            "Learning Mode"
        findViewById<View>(R.id.itemAbout).findViewById<TextView>(R.id.title).text =
            "About"
    }


    private fun setupNavigationItems() {

        findViewById<View>(R.id.itemEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<View>(R.id.itemChangeAvatar).setOnClickListener {
            avatarPickerLauncher.launch(Intent(this, AvatarPickerActivity::class.java))
        }

        findViewById<View>(R.id.itemAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
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
        val avatar = AvatarRepository.load(this).firstOrNull { it.id == avatarId } ?: return

        val resId = AvatarRepository.resolveDrawableResId(this, avatar.drawableName)
        if (resId != 0) avatarIv.setImageResource(resId)
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
        userListener = null
    }
}