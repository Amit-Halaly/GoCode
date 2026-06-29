package com.example.gocode.market

import com.example.gocode.models.AvatarItem
import com.example.gocode.repositories.AvatarRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class MarketProfile(
    val coins: Long,
    val ownedAvatarIds: Set<String>,
    val equippedAvatarId: String
)

sealed class MarketActionResult {
    data class Success(val profile: MarketProfile) : MarketActionResult()
    data class Failure(val message: String) : MarketActionResult()
}

object MarketRepository {
    private const val KEY_COINS = "coins"
    private const val KEY_AVATAR_ID = "avatarId"
    private const val KEY_OWNED_AVATARS = "ownedAvatarIds"

    fun loadProfile(
        avatars: List<AvatarItem>,
        onResult: (MarketProfile?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(null)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toMarketProfile(avatars))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun buyAvatar(
        avatars: List<AvatarItem>,
        avatar: AvatarItem,
        onResult: (MarketActionResult) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(MarketActionResult.Failure("Sign in to use the market."))
            return
        }

        val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val owned = snapshot.ownedAvatarIds(avatars).toMutableSet()
            val coins = snapshot.getLong(KEY_COINS) ?: 0L

            if (avatar.id in owned) {
                return@runTransaction MarketActionResult.Success(snapshot.toMarketProfile(avatars))
            }

            if (coins < avatar.price) {
                return@runTransaction MarketActionResult.Failure("Not enough coins yet.")
            }

            owned.add(avatar.id)
            val updatedCoins = coins - avatar.price
            val profile = MarketProfile(
                coins = updatedCoins,
                ownedAvatarIds = owned,
                equippedAvatarId = avatar.id
            )

            transaction.set(
                userRef,
                mapOf(
                    KEY_COINS to updatedCoins,
                    KEY_OWNED_AVATARS to owned.toList(),
                    KEY_AVATAR_ID to avatar.id
                ),
                SetOptions.merge()
            )
            MarketActionResult.Success(profile)
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener {
            onResult(MarketActionResult.Failure("Purchase failed. Try again."))
        }
    }

    fun equipAvatar(
        avatars: List<AvatarItem>,
        avatarId: String,
        onResult: (MarketActionResult) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(MarketActionResult.Failure("Sign in to change avatar."))
            return
        }

        val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val owned = snapshot.ownedAvatarIds(avatars)
            if (avatarId !in owned) {
                return@runTransaction MarketActionResult.Failure("This avatar is still locked.")
            }

            transaction.set(userRef, mapOf(KEY_AVATAR_ID to avatarId), SetOptions.merge())
            MarketActionResult.Success(
                MarketProfile(
                    coins = snapshot.getLong(KEY_COINS) ?: 0L,
                    ownedAvatarIds = owned,
                    equippedAvatarId = avatarId
                )
            )
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener {
            onResult(MarketActionResult.Failure("Could not equip avatar."))
        }
    }

    private fun DocumentSnapshot.toMarketProfile(avatars: List<AvatarItem>): MarketProfile {
        val owned = ownedAvatarIds(avatars)
        val requestedAvatarId = getString(KEY_AVATAR_ID)
        val equippedAvatarId = requestedAvatarId
            ?.takeIf { it in owned }
            ?: AvatarRepository.DEFAULT_AVATAR_ID

        return MarketProfile(
            coins = getLong(KEY_COINS) ?: 0L,
            ownedAvatarIds = owned,
            equippedAvatarId = equippedAvatarId
        )
    }

    private fun DocumentSnapshot.ownedAvatarIds(avatars: List<AvatarItem>): Set<String> {
        val cloudOwned = (get(KEY_OWNED_AVATARS) as? List<*>)
            .orEmpty()
            .mapNotNull { it as? String }
        return AvatarRepository.defaultOwnedAvatarIds(avatars) + cloudOwned
    }
}
