package com.example.gocode.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.adapters.AvatarAdapter
import com.example.gocode.market.MarketRepository
import com.example.gocode.repositories.AvatarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AvatarPickerActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_picker)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recycler = findViewById<RecyclerView>(R.id.avatarRecycler)
        recycler.layoutManager = GridLayoutManager(this, 3)

        val user = auth.currentUser ?: return
        val avatars = AvatarRepository.load(this)

        MarketRepository.loadProfile(avatars) { profile ->
            val defaultOwnedIds = AvatarRepository.defaultOwnedAvatarIds(avatars)
            val ownedIds = profile?.ownedAvatarIds ?: defaultOwnedIds
            val currentAvatarId = profile?.equippedAvatarId ?: AvatarRepository.DEFAULT_AVATAR_ID

            if (currentAvatarId == AvatarRepository.DEFAULT_AVATAR_ID) {
                db.collection("users")
                    .document(user.uid)
                    .set(
                        mapOf(
                            "avatarId" to AvatarRepository.DEFAULT_AVATAR_ID,
                            "ownedAvatarIds" to ownedIds.toList()
                        ),
                        SetOptions.merge()
                    )
            }

            recycler.adapter = AvatarAdapter(
                items = avatars,
                initiallySelectedId = currentAvatarId,
                unlockedIds = ownedIds
            ) { selectedAvatar ->

                db.collection("users")
                    .document(user.uid)
                    .update("avatarId", selectedAvatar.id)

                val result = Intent().apply {
                    putExtra("selectedAvatarId", selectedAvatar.id)
                }

                setResult(RESULT_OK, result)
                finish()
            }
        }
    }
}
