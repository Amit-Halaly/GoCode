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
    val friendCode: String,
    val avatarId: String?,
    val rating: Int,
    val primaryLanguage: String,
    val onlineStatus: String,
    val arenaState: String,
    val level: Int,
    val xp: Int,
    val arenaWins: Int,
    val challengesSolved: Int,
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
    val friendCode: String,
    val avatarId: String?,
    val rating: Int,
    val primaryLanguage: String,
)

data class ArenaInvite(
    val id: String,
    val fromUid: String,
    val toUid: String,
    val fromUsername: String,
    val inviteCode: String,
    val fromRating: Int,
    val fromAvatarId: String?,
    val fromLanguages: List<String>,
    val status: String,
)

object FriendsRepository {
    private const val USERS = "users"
    private const val FRIEND_REQUESTS = "friendRequests"
    private const val FRIENDS = "friends"
    private const val ARENA_INVITES = "arenaInvites"
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
            val friendCode = friendCodeForUid(user.uid)
            ref.set(
                mapOf(
                    "username" to username,
                    "usernameLower" to username.lowercase(),
                    "friendCode" to friendCode,
                    "friendCodeSearch" to friendCode.normalizedSearch(),
                    "email" to (user.email ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge()
            )
        }
    }

    fun listenCurrentUserFriendCode(
        onChanged: (String) -> Unit,
        onError: (Exception) -> Unit,
    ): ListenerRegistration? {
        val user = auth.currentUser ?: return null
        return db.collection(USERS).document(user.uid).addSnapshotListener { doc, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            onChanged(doc?.getString("friendCode") ?: friendCodeForUid(user.uid))
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
        val codeSearch = query.normalizedSearch()
        if (normalized.isBlank()) {
            onResult(emptyList())
            return
        }

        val results = linkedMapOf<String, FriendSearchResult>()
        var completed = 0
        var reported = false

        fun finish(snapshotResults: List<DocumentSnapshot>) {
            snapshotResults
                .filter { it.id != currentUid }
                .map { it.toSearchResult() }
                .forEach { results[it.uid] = it }
            completed++
            if (completed == 3 && !reported) {
                reported = true
                onResult(results.values.sortedBy { it.username.lowercase() })
            }
        }

        fun fail(error: Exception) {
            if (!reported) {
                reported = true
                onError(error)
            }
        }

        db.collection(USERS)
            .whereGreaterThanOrEqualTo("usernameLower", normalized)
            .whereLessThanOrEqualTo("usernameLower", normalized + "\uf8ff")
            .limit(12)
            .get()
            .addOnSuccessListener { snapshot ->
                finish(snapshot.documents)
            }
            .addOnFailureListener(::fail)

        db.collection(USERS)
            .whereGreaterThanOrEqualTo("username", query.trim())
            .whereLessThanOrEqualTo("username", query.trim() + "\uf8ff")
            .limit(12)
            .get()
            .addOnSuccessListener { snapshot ->
                finish(snapshot.documents)
            }
            .addOnFailureListener(::fail)

        db.collection(USERS)
            .whereEqualTo("friendCodeSearch", codeSearch)
            .limit(3)
            .get()
            .addOnSuccessListener { snapshot ->
                finish(snapshot.documents)
            }
            .addOnFailureListener(::fail)
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

    fun sendArenaInvite(
        targetUid: String,
        inviteCode: String,
        onComplete: (Boolean, String) -> Unit,
    ) {
        val currentUid = auth.currentUser?.uid ?: run {
            onComplete(false, "Sign in required")
            return
        }
        if (targetUid == currentUid) {
            onComplete(false, "Choose a friend to invite")
            return
        }

        val currentRef = db.collection(USERS).document(currentUid)
        currentRef.get()
            .addOnSuccessListener { current ->
                val primaryLanguage = current.getString("primaryLanguage") ?: "Java"
                val knownLanguages = current.get("knownLanguages") as? List<*>
                val languages = (knownLanguages?.filterIsInstance<String>().orEmpty() + primaryLanguage)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .ifEmpty { listOf("Java") }
                val inviteRef = db.collection(ARENA_INVITES).document()
                inviteRef.set(
                    mapOf(
                        "fromUid" to currentUid,
                        "toUid" to targetUid,
                        "fromUsername" to current.displayName(),
                        "inviteCode" to inviteCode,
                        "fromRating" to (current.getLong("rating") ?: 1000L),
                        "fromAvatarId" to (current.getString("avatarId") ?: ""),
                        "fromLanguages" to languages,
                        "status" to STATUS_PENDING,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                    .addOnSuccessListener { onComplete(true, "Arena invite sent") }
                    .addOnFailureListener { onComplete(false, it.message ?: "Could not send invite") }
            }
            .addOnFailureListener { onComplete(false, it.message ?: "Could not send invite") }
    }

    fun listenIncomingArenaInvites(
        onChanged: (List<ArenaInvite>) -> Unit,
        onError: (Exception) -> Unit,
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection(ARENA_INVITES)
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", STATUS_PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChanged(snapshot?.documents.orEmpty().map { it.toArenaInvite() })
            }
    }

    fun updateArenaInviteStatus(
        inviteId: String,
        accepted: Boolean,
        onComplete: (Boolean, String) -> Unit,
    ) {
        val status = if (accepted) STATUS_ACCEPTED else STATUS_DECLINED
        db.collection(ARENA_INVITES).document(inviteId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
            .addOnSuccessListener { onComplete(true, if (accepted) "Joining Arena" else "Invite declined") }
            .addOnFailureListener { onComplete(false, it.message ?: "Could not update invite") }
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
            friendCode = getString("friendCode") ?: friendCodeForUid(id),
            avatarId = getString("avatarId"),
            rating = (getLong("rating") ?: 0L).toInt(),
            primaryLanguage = getString("primaryLanguage") ?: "Java",
            onlineStatus = getString("onlineStatus") ?: "offline",
            arenaState = getString("arenaState") ?: "idle",
            level = (getLong("level") ?: 1L).toInt(),
            xp = (getLong("xp") ?: 0L).toInt(),
            arenaWins = (getLong("arenaWins") ?: 0L).toInt(),
            challengesSolved = (
                (getLong("practiceNodesCompleted") ?: 0L) +
                    (getLong("codeNodesCompleted") ?: 0L)
                ).toInt(),
            lastActiveAtText = if (getTimestamp("lastActiveAt") == null) "Unknown" else "Recently active",
        )
    }

    private fun DocumentSnapshot.toSearchResult(): FriendSearchResult {
        return FriendSearchResult(
            uid = id,
            username = displayName(),
            friendCode = getString("friendCode") ?: friendCodeForUid(id),
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

    private fun DocumentSnapshot.toArenaInvite(): ArenaInvite {
        return ArenaInvite(
            id = id,
            fromUid = getString("fromUid").orEmpty(),
            toUid = getString("toUid").orEmpty(),
            fromUsername = getString("fromUsername") ?: "Friend",
            inviteCode = getString("inviteCode").orEmpty(),
            fromRating = (getLong("fromRating") ?: 1000L).toInt(),
            fromAvatarId = getString("fromAvatarId")?.takeIf { it.isNotBlank() },
            fromLanguages = (get("fromLanguages") as? List<*>)
                ?.filterIsInstance<String>()
                ?.filter { it.isNotBlank() }
                .orEmpty(),
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
            "friendCode" to (getString("friendCode") ?: friendCodeForUid(id)),
            "avatarId" to (getString("avatarId") ?: ""),
            "rating" to (getLong("rating") ?: 0L),
            "primaryLanguage" to (getString("primaryLanguage") ?: "Java"),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
    }

    fun friendCodeForUid(uid: String): String {
        val suffix = uid.filter { it.isLetterOrDigit() }
            .take(8)
            .uppercase()
            .padEnd(8, '0')
        return "GC-$suffix"
    }

    private fun String.normalizedSearch(): String = filter { it.isLetterOrDigit() }.lowercase()
}
