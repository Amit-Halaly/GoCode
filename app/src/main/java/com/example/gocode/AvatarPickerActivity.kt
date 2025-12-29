package com.example.gocode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.adapters.AvatarAdapter
import com.example.gocode.repositories.AvatarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AvatarPickerActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_picker)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recycler = findViewById<RecyclerView>(R.id.avatarRecycler)
        recycler.layoutManager = GridLayoutManager(this, 3)

        val user = auth.currentUser ?: return
        val avatars = AvatarRepository.load(this)

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                val currentAvatarId = doc.getString("avatarId") ?: ""

                recycler.adapter = AvatarAdapter(
                    items = avatars,
                    initiallySelectedId = currentAvatarId
                ) { selectedAvatar ->

                    db.collection("users")
                        .document(user.uid)
                        .update("avatarId", selectedAvatar.id)

                    val result = Intent().apply {
                        putExtra("selectedAvatarId", selectedAvatar.id)
                    }

                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }
    }
}
