package com.example.gocode.friends

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gocode.R
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ListenerRegistration

class FriendsActivity : AppCompatActivity() {

    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null
    private var currentCodeListener: ListenerRegistration? = null
    private var currentFriendCode: String = ""

    private lateinit var subtitle: TextView
    private lateinit var myFriendCodeText: TextView
    private lateinit var requestsPanel: View
    private lateinit var requestsContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var searchEmpty: TextView
    private lateinit var searchResults: LinearLayout
    private lateinit var friendsList: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        findViewById<ImageView>(R.id.friendsBackButton).setOnClickListener { finish() }
        subtitle = findViewById(R.id.friendsSubtitle)
        myFriendCodeText = findViewById(R.id.myFriendCodeText)
        requestsPanel = findViewById(R.id.friendRequestsPanel)
        requestsContainer = findViewById(R.id.friendRequestsContainer)
        searchInput = findViewById(R.id.friendsSearchInput)
        searchEmpty = findViewById(R.id.friendsSearchEmpty)
        searchResults = findViewById(R.id.friendsSearchResults)
        friendsList = findViewById(R.id.friendsList)
        emptyText = findViewById(R.id.friendsEmptyText)

        findViewById<MaterialButton>(R.id.friendsSearchButton).setOnClickListener {
            executeSearch()
        }
        findViewById<MaterialButton>(R.id.copyFriendCodeButton).setOnClickListener {
            copyFriendCode()
        }
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                executeSearch()
                true
            } else {
                false
            }
        }

        FriendsRepository.syncCurrentUserSearchFields()
        FriendsRepository.syncCurrentUserPresence()
        listenToCurrentCode()
        listenToFriends()
        listenToRequests()
    }

    private fun listenToCurrentCode() {
        currentCodeListener = FriendsRepository.listenCurrentUserFriendCode(
            onChanged = { code ->
                runOnUiThread {
                    currentFriendCode = code
                    myFriendCodeText.text = "My code: $code"
                }
            },
            onError = { error -> runOnUiThread { showError(error.message) } },
        )
    }

    private fun listenToFriends() {
        friendsListener = FriendsRepository.listenFriends(
            onChanged = { friends -> runOnUiThread { renderFriends(friends) } },
            onError = { error -> runOnUiThread { showError(error.message) } },
        )
    }

    private fun listenToRequests() {
        requestsListener = FriendsRepository.listenIncomingRequests(
            onChanged = { requests -> runOnUiThread { renderRequests(requests) } },
            onError = { error -> runOnUiThread { showError(error.message) } },
        )
    }

    private fun renderFriends(friends: List<FriendProfile>) {
        friendsList.removeAllViews()
        subtitle.text = when (friends.size) {
            0 -> "Add friends to compare progress and jump into Arena"
            1 -> "1 friend in your crew"
            else -> "${friends.size} friends in your crew"
        }
        emptyText.visibility = if (friends.isEmpty()) View.VISIBLE else View.GONE
        if (friends.isEmpty()) {
            emptyText.text = "No friends yet"
            return
        }

        friends.forEach { friend ->
            friendsList.addView(friendRow(friend))
        }
    }

    private fun renderRequests(requests: List<FriendRequest>) {
        requestsContainer.removeAllViews()
        requestsPanel.visibility = if (requests.isEmpty()) View.GONE else View.VISIBLE
        requests.forEach { request ->
            requestsContainer.addView(requestRow(request))
        }
    }

    private fun executeSearch() {
        val query = searchInput.text.toString().trim()
        searchResults.removeAllViews()
        if (query.isBlank()) {
            searchEmpty.text = "Type a username or friend code first"
            searchEmpty.visibility = View.VISIBLE
            return
        }

        searchEmpty.text = "Searching..."
        searchEmpty.visibility = View.VISIBLE
        FriendsRepository.searchUsers(
            query = query,
            onResult = { users ->
                runOnUiThread {
                    renderSearchResults(users)
                }
            },
            onError = { error ->
                runOnUiThread {
                    searchEmpty.text = error.message ?: "Search unavailable"
                    searchEmpty.visibility = View.VISIBLE
                }
            },
        )
    }

    private fun renderSearchResults(users: List<FriendSearchResult>) {
        searchResults.removeAllViews()
        if (users.isEmpty()) {
            searchEmpty.text = "No users found"
            searchEmpty.visibility = View.VISIBLE
            return
        }

        searchEmpty.visibility = View.GONE
        users.forEach { user ->
            searchResults.addView(searchResultRow(user))
        }
    }

    private fun friendRow(friend: FriendProfile): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundResource(R.drawable.bg_cards)
            isClickable = true
            isFocusable = true
            setOnClickListener { showFriendDetails(friend) }

            addView(ImageView(this@FriendsActivity).apply {
                setImageResource(avatarRes(friend.avatarId))
                setBackgroundResource(R.drawable.bg_avatar_circle)
                setPadding(dp(7), dp(7), dp(7), dp(7))
            }, LinearLayout.LayoutParams(dp(58), dp(58)))

            addView(LinearLayout(this@FriendsActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)

                addView(TextView(this@FriendsActivity).apply {
                    text = friend.username
                    setTextColor(ContextCompat.getColor(context, R.color.gc_text_primary))
                    textSize = 17f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                })
                addView(TextView(this@FriendsActivity).apply {
                    text = statusLine(friend)
                    setTextColor(ContextCompat.getColor(context, R.color.gc_text_secondary))
                    textSize = 13f
                })
                addView(TextView(this@FriendsActivity).apply {
                    text = "${friend.primaryLanguage} - Rating ${friend.rating}"
                    setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                    textSize = 12f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                })
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }.withBottomMargin()
    }

    private fun showFriendDetails(friend: FriendProfile) {
        val details = listOf(
            statusLine(friend),
            "${friend.primaryLanguage} - Rating ${friend.rating}",
            "Level ${friend.level} - XP ${friend.xp}",
            "Arena wins ${friend.arenaWins}",
            "Challenges solved ${friend.challengesSolved}",
        ).joinToString("\n")

        AlertDialog.Builder(this)
            .setTitle(friend.username)
            .setMessage(details)
            .setNegativeButton("Close", null)
            .setPositiveButton("Remove friend") { _, _ ->
                FriendsRepository.removeFriend(friend.uid) { success, message ->
                    runOnUiThread { showActionResult(success, message) }
                }
            }
            .show()
    }

    private fun searchResultRow(user: FriendSearchResult): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundResource(R.drawable.bg_arena_leaderboard_row)

            addView(ImageView(this@FriendsActivity).apply {
                setImageResource(avatarRes(user.avatarId))
                setBackgroundResource(R.drawable.bg_avatar_circle)
                setPadding(dp(6), dp(6), dp(6), dp(6))
            }, LinearLayout.LayoutParams(dp(48), dp(48)))

            addView(LinearLayout(this@FriendsActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), 0, dp(10), 0)
                addView(TextView(this@FriendsActivity).apply {
                    text = user.username
                    setTextColor(ContextCompat.getColor(context, R.color.gc_text_primary))
                    textSize = 15f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                })
                addView(TextView(this@FriendsActivity).apply {
                    text = "${user.friendCode} - ${user.primaryLanguage} - Rating ${user.rating}"
                    setTextColor(ContextCompat.getColor(context, R.color.gc_text_secondary))
                    textSize = 12f
                })
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            addView(actionButton("Add", R.color.accent_green) {
                FriendsRepository.sendFriendRequest(user.uid) { success, message ->
                    runOnUiThread {
                        showActionResult(success, message)
                        if (success) executeSearch()
                    }
                }
            }, LinearLayout.LayoutParams(dp(92), dp(42)))
        }.withBottomMargin()
    }

    private fun requestRow(request: FriendRequest): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundResource(R.drawable.bg_arena_leaderboard_row)

            addView(TextView(this@FriendsActivity).apply {
                text = "${request.fromUsername} wants to be friends"
                setTextColor(ContextCompat.getColor(context, R.color.gc_text_primary))
                textSize = 15f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })

            addView(LinearLayout(this@FriendsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(8), 0, 0)
                addView(actionButton("Accept", R.color.accent_green) {
                    FriendsRepository.acceptRequest(request, ::showActionResult)
                }, LinearLayout.LayoutParams(0, dp(42), 1f).apply { marginEnd = dp(6) })
                addView(actionButton("Decline", R.color.gc_danger) {
                    FriendsRepository.declineRequest(request, ::showActionResult)
                }, LinearLayout.LayoutParams(0, dp(42), 1f).apply { marginStart = dp(6) })
            })
        }.withBottomMargin()
    }

    private fun actionButton(
        label: String,
        colorRes: Int,
        onClick: () -> Unit,
    ): MaterialButton {
        return MaterialButton(this).apply {
            text = label
            isAllCaps = false
            minWidth = 0
            minHeight = 0
            setPadding(dp(8), 0, dp(8), 0)
            textSize = 13f
            cornerRadius = dp(8)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, colorRes)
            )
            setOnClickListener { onClick() }
        }
    }

    private fun statusLine(friend: FriendProfile): String {
        return when {
            friend.arenaState == "matchmaking" -> "Looking for an Arena match"
            friend.arenaState == "in_match" -> "In an Arena match"
            friend.onlineStatus == "online" -> "Online"
            else -> "Offline - ${friend.lastActiveAtText}"
        }
    }

    private fun avatarRes(avatarId: String?): Int {
        val avatar = AvatarRepository.load(this).firstOrNull { it.id == avatarId }
        if (avatar != null) {
            val resId = AvatarRepository.resolveDrawableResId(this, avatar.drawableName)
            if (resId != 0) return resId
        }
        return R.drawable.avatar_robot
    }

    private fun showActionResult(success: Boolean, message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun copyFriendCode() {
        if (currentFriendCode.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("GoCode friend code", currentFriendCode))
        Toast.makeText(this, "Friend code copied", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Friends unavailable", Toast.LENGTH_SHORT).show()
    }

    private fun View.withBottomMargin(): View {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(10)
        }
        return this
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        friendsListener?.remove()
        requestsListener?.remove()
        currentCodeListener?.remove()
    }
}
