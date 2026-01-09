package com.example.gocode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gocode.repositories.AvatarRepository
import com.example.gocode.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileFragment : Fragment() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var userListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val achFirstLogin = view.findViewById<ImageView>(R.id.achFirstLogin)
        val achFirstCourse = view.findViewById<ImageView>(R.id.achFirstCourse)
        val achFirstChallenge = view.findViewById<ImageView>(R.id.achFirstChallenge)
        val achChallenges10 = view.findViewById<ImageView>(R.id.achChallenges10)
        val achArenaFirstWin = view.findViewById<ImageView>(R.id.achArenaFirstWin)
        val achStreak7 = view.findViewById<ImageView>(R.id.achStreak7)

        val btnProfileMenu = view.findViewById<ImageButton>(R.id.btnProfileMenu)

        val avatarIv = view.findViewById<ImageView>(R.id.avatarImage)
        val usernameTv = view.findViewById<TextView>(R.id.profileUsername)
        val levelTv = view.findViewById<TextView>(R.id.profileLevelText)
        val xpTv = view.findViewById<TextView>(R.id.profileXpText)
        val xpProgress = view.findViewById<ProgressBar>(R.id.profileXpProgress)

        val coursesCompletedTv = view.findViewById<TextView>(R.id.profileCoursesCompleted)
        val challengesSolvedTv = view.findViewById<TextView>(R.id.profileChallengesSolved)
        val arenaWinsTv = view.findViewById<TextView>(R.id.profileArenaWins)

        coursesCompletedTv.text = "0"
        challengesSolvedTv.text = "0"
        arenaWinsTv.text = "0"

        val user = auth.currentUser ?: return

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            doc.getString("username")?.takeIf { it.isNotBlank() }?.let { usernameTv.text = it }

            doc.getString("avatarId")?.takeIf { it.isNotBlank() }?.let { avatarId ->
                val avatars = AvatarRepository.load(requireContext())
                val avatarItem = avatars.firstOrNull { it.id == avatarId }
                if (avatarItem != null) {
                    val resId = AvatarRepository.resolveDrawableResId(
                        requireContext(), avatarItem.drawableName
                    )
                    if (resId != 0) avatarIv.setImageResource(resId)
                }
            }

            val level = doc.getLong("level") ?: 1L
            levelTv.text = "level $level"

            val xp = doc.getLong("xp") ?: 0L
            val xpToNext = doc.getLong("xpToNext") ?: 1000L
            xpTv.text = "XP $xp/$xpToNext"

            val max = xpToNext.toInt().coerceAtLeast(1)
            xpProgress.max = max
            xpProgress.progress = xp.toInt().coerceIn(0, max)
        }

        btnProfileMenu.setOnClickListener {
            val popup = PopupMenu(view.context, btnProfileMenu)
            popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

            val logoutItem = popup.menu.findItem(R.id.action_logout)
            val redTitle = android.text.SpannableString(logoutItem.title)
            redTitle.setSpan(
                android.text.style.ForegroundColorSpan(
                    resources.getColor(R.color.profile_lo_red, null)
                ),
                0,
                redTitle.length,
                0
            )
            logoutItem.title = redTitle


            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.action_settings -> {
                        startActivity(
                            Intent(requireContext(), SettingsActivity::class.java)
                        )
                        true
                    }


                    R.id.action_logout -> {
                        auth.signOut()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }


        view.post {
            val bottomNav =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            val navHeight = bottomNav.height
            view.setPadding(
                view.paddingLeft, view.paddingTop, view.paddingRight, navHeight
            )
        }

        achFirstLogin.setOnClickListener {
            showAchievement(
                R.drawable.ach_first_login,
                "First Login",
                "You logged into GoCode for the first time."
            )
        }

        achFirstCourse.setOnClickListener {
            showAchievement(
                R.drawable.ach_first_course, "First Course", "You completed your first course."
            )
        }

        achFirstChallenge.setOnClickListener {
            showAchievement(
                R.drawable.ach_first_challenge,
                "First Challenge",
                "You solved your first coding challenge."
            )
        }

        achChallenges10.setOnClickListener {
            showAchievement(
                R.drawable.ach_challenges_10,
                "10 Challenges",
                "Solve 10 challenges to unlock this achievement."
            )
        }

        achArenaFirstWin.setOnClickListener {
            showAchievement(
                R.drawable.ach_arena_first_win, "Arena Victory", "Win your first Arena match."
            )
        }

        achStreak7.setOnClickListener {
            showAchievement(
                R.drawable.ach_streak_7, "7 Day Streak", "Log in 7 days in a row."
            )
        }

    }

    private fun showAchievement(
        icon: Int, title: String, desc: String
    ) {
        AchievementBottomSheet.newInstance(icon, title, desc)
            .show(parentFragmentManager, "achievement_bs")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        userListener = null
    }
}
