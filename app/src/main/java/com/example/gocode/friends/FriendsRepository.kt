package com.example.gocode.friends

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

data class FriendProfile(
    val uid: String,
    val username: String,
    val avatarId: String?,
    val rating: Int,
    val primaryLanguage: String,
    val onlineStatus: String,
    val arenaState: String,
    val lastActiveAtText: String,
)

data class FriendRequest(
    val id: String,
    val fromUid: String,
    val toUid: String,
    val fromUsername: String,
    val toUsername: String,
    val status: String,
)

data class FriendSearchResult(
    val uid: String,
    val username: String,
    val avatarId: String?,
    val rating: Int,
    val primaryLanguage: String,
)

object FriendsRepository {
    private const val USERS = "users"
    private const val FRIEND_REQUESTS = "friendRequests"
    private const val FRIENDS = "friends"
    private const val STATUS_PENDING = "pending"
    private const val STATUS_ACCEPTED = "accepted"
    private const val STATUS_DECLINED = "declined"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun syncCurrentUserPresence(arenaState: String = "idle") {
        val user = auth.currentUser ?: return
        db.collection(USERS).document(user.uid).set(
            mapOf(
                "email" to (user.email ?: ""),
                "onlineStatus" to "online",
                "arenaState" to arenaState,
                "lastActiveAt" to FieldValue.serverTimestamp(),
            ),
            SetOptions.merge()
        )
    }

    fun syncCurrentUserSearchFields() {
        val user = auth.currentUser ?: return
        val ref = db.collection(USERS).document(user.uid)
        ref.get().addOnSuccessListener { doc ->
            val username = doc.getString("username")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: user.displayName
                ?: user.email?.substringBefore("@")
                ?: "Player"
            ref.set(
                mapOf(
                    "username" to username,
                    "usernameLower" to username.lowercase(),
                    "friendCode" to username.lowercase().filter { it.isLetterOrDigit() },
                    "email" to (user.email ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge()
            )
        }
    }

    fun listenFriends(
        onChanged: (List<FriendProfile>) -> Unit,
        onError: (Exception) -> Unit,
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection(USERS)
            .document(uid)
            .collection(FRIENDS)
            .orderBy("username", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val friendIds = snapshot?.documents
                    ?.mapNotNull { it.getString("friendUid") ?: it.id }
                    .orEmpty()
                loadProfiles(friendIds, onChanged, onError)
            }
    }

    fun listenIncomingRequests(
        onChanged: (List<FriendRequest>) -> Unit,
        onError: (Exception) -> Unit,
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection(FRIEND_REQUESTS)
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", STATUS_PENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChanged(snapshot?.documents.orEmpty().map { it.toFriendRequest() })
            }
    }

    fun searchUsers(
        query: String,
        onResult: (List<FriendSearchResult>) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val currentUid = auth.currentUser?.uid
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) {
            onResult(emptyList())
            return
        }

        db.collection(USERS)
            .whereGreaterThanOrEqualTo("usernameLower", normalized)
            .whereLessThanOrEqualTo("usernameLower", normalized + "\uf8ff")
            .limit(12)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.documents
                        .filter { it.id != currentUid }
                        .map { it.toSearchResult() }
                )
            }
            .addOnFailureListener(onError)
    }

    fun sendFriendRequest(
        targetUid: String,
        onComplete: (Boolean, String) -> Unit,
    ) {
        val currentUid = auth.currentUser?.uid ?: run {
            onComplete(false, "Sign in required")
            return
        }
        if (currentUid == targetUid) {
            onComplete(false, "You cannot add yourself")
            return
        }

        db.runTransaction { transaction ->
            val currentRef = db.collection(USERS).document(currentUid)
            val targetRef = db.collection(USERS).document(targetUid)
            val current = transaction.get(currentRef)
            val target = transaction.get(targetRef)
            if (!target.exists()) error("User not found")

            val requestId = requestId(currentUid, targetUid)
            val reverseRequestId = requestId(targetUid, currentUid)
            val requestRef = db.collection(FRIEND_REQUESTS).document(requestId)
            val reverseRequestRef = db.collection(FRIEND_REQUESTS).document(reverseRequestId)
            val existing = transaction.get(requestRef)
            val reverseExisting = transaction.get(reverseRequestRef)
            val alreadyFriends = transaction.get(
                currentRef.collection(FRIENDS).document(targetUid)
            ).exists()

            if (alreadyFriends) error("Already friends")
            if (existing.exists() && existing.getString("status") == STATUS_PENDING) {
                error("Request already sent")
            }
            if (reverseExisting.exists() && reverseExisting.getString("status") == STATUS_PENDING) {
                error("This user already sent you a request")
            }

            transaction.set(
                requestRef,
                mapOf(
                    "fromUid" to currentUid,
                    "toUid" to targetUid,
                    "fromUsername" to current.displayName(),
                    "toUsername" to target.displayName(),
                    "status" to STATUS_PENDING,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
        }.addOnSuccessListener {
            onComplete(true, "Request sent")
        }.addOnFailureListener { error ->
            onComplete(false, error.message ?: "Could not send request")
        }
    }

    fun acceptRequest(
        request: FriendRequest,
        onComplete: (Boolean, String) -> Unit,
    ) {
        val currentUid = auth.currentUser?.uid ?: run {
            onComplete(false, "Sign in required")
            return
        }
        if (request.toUid != currentUid) {
            onComplete(false, "Request no longer available")
            return
        }

        db.runTransaction { transaction ->
            val requestRef = db.collection(FRIEND_REQUESTS).document(request.id)
            val requestSnapshot = transaction.get(requestRef)
            if (!requestSnapshot.exists() || requestSnapshot.getString("status") != STATUS_PENDING) {
                error("Request no longer available")
            }

            val fromRef = db.collection(USERS).document(request.fromUid)
            val toRef = db.collection(USERS).document(request.toUid)
            val from = transaction.get(fromRef)
            val to = transaction.get(toRef)

            transaction.set(fromRef.collection(FRIENDS).document(request.toUid), to.friendMap(request.fromUid))
            transaction.set(toRef.collection(FRIENDS).document(request.fromUid), from.friendMap(request.toUid))
            transaction.update(
                requestRef,
                mapOf(
                    "status" to STATUS_ACCEPTED,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
        }.addOnSuccessListener {
            onComplete(true, "Friend added")
        }.addOnFailureListener { error ->
            onComplete(false, error.message ?: "Could not accept request")
        }
    }

    fun declineRequest(
        request: FriendRequest,
        onComplete: (Boolean, String) -> Unit,
    ) {
        db.collection(FRIEND_REQUESTS).document(request.id)
            .update(
                mapOf(
                    "status" to STATUS_DECLINED,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
            .addOnSuccessListener { onComplete(true, "Request declined") }
            .addOnFailureListener { onComplete(false, it.message ?: "Could not decline request") }
    }

    fun removeFriend(
        friendUid: String,
        onComplete: (Boolean, String) -> Unit,
    ) {
        val currentUid = auth.currentUser?.uid ?: run {
            onComplete(false, "Sign in required")
            return
        }
        db.runBatch { batch ->
            batch.delete(db.collection(USERS).document(currentUid).collection(FRIENDS).document(friendUid))
            batch.delete(db.collection(USERS).document(friendUid).collection(FRIENDS).document(currentUid))
        }.addOnSuccessListener {
            onComplete(true, "Friend removed")
        }.addOnFailureListener {
            onComplete(false, it.message ?: "Could not remove friend")
        }
    }

    private fun loadProfiles(
        friendIds: List<String>,
        onChanged: (List<FriendProfile>) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        if (friendIds.isEmpty()) {
            onChanged(emptyList())
            return
        }

        val chunks = friendIds.distinct().chunked(10)
        val loaded = mutableListOf<FriendProfile>()
        var completed = 0

        chunks.forEach { ids ->
            db.collection(USERS)
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener { snapshot ->
                    loaded += snapshot.documents.map { it.toFriendProfile() }
                    completed++
                    if (completed == chunks.size) {
                        onChanged(loaded.sortedBy { it.username.lowercase() })
                    }
                }
                .addOnFailureListener(onError)
        }
    }

    private fun requestId(fromUid: String, toUid: String): String = "${fromUid}_$toUid"

    private fun DocumentSnapshot.toFriendProfile(): FriendProfile {
        return FriendProfile(
            uid = id,
            username = displayName(),
            avatarId = getString("avatarId"),
            rating = (getLong("rating") ?: 0L).toInt(),
            primaryLanguage = getString("primaryLanguage") ?: "Java",
            onlineStatus = getString("onlineStatus") ?: "offline",
            arenaState = getString("arenaState") ?: "idle",
            lastActiveAtText = if (getTimestamp("lastActiveAt") == null) "Unknown" else "Recently active",
        )
    }

    private fun DocumentSnapshot.toSearchResult(): FriendSearchResult {
        return FriendSearchResult(
            uid = id,
            username = displayName(),
            avatarId = getString("avatarId"),
            rating = (getLong("rating") ?: 0L).toInt(),
            primaryLanguage = getString("primaryLanguage") ?: "Java",
        )
    }

    private fun DocumentSnapshot.toFriendRequest(): FriendRequest {
        return FriendRequest(
            id = id,
            fromUid = getString("fromUid").orEmpty(),
            toUid = getString("toUid").orEmpty(),
            fromUsername = getString("fromUsername").orEmpty(),
            toUsername = getString("toUsername").orEmpty(),
            status = getString("status") ?: STATUS_PENDING,
        )
    }

    private fun DocumentSnapshot.displayName(): String {
        return getString("username")?.takeIf { it.isNotBlank() }
            ?: getString("email")?.substringBefore("@")
            ?: "Player"
    }

    private fun DocumentSnapshot.friendMap(friendUid: String): Map<String, Any> {
        return mapOf(
            "friendUid" to id,
            "username" to displayName(),
            "avatarId" to (getString("avatarId") ?: ""),
            "rating" to (getLong("rating") ?: 0L),
            "primaryLanguage" to (getString("primaryLanguage") ?: "Java"),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
    }
}
