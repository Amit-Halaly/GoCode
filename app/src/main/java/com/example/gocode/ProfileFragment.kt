package com.example.gocode

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gocode.gamification.AchievementCatalog
import com.example.gocode.gamification.AchievementDefinition
import com.example.gocode.gamification.GamificationRepository
import com.example.gocode.lessons.LessonProgressStore
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

        val btnProfileMenu = view.findViewById<ImageButton>(R.id.btnProfileMenu)

        val avatarWithStatus = view.findViewById<View>(R.id.avatarWithStatus)
        val avatarIv = avatarWithStatus.findViewById<ImageView>(R.id.avatarImage)
        val statusDot = avatarWithStatus.findViewById<View>(R.id.avatarStatusDot)

        val usernameTv = view.findViewById<TextView>(R.id.profileUsername)
        val levelTv = view.findViewById<TextView>(R.id.profileLevelText)
        val xpTv = view.findViewById<TextView>(R.id.profileXpText)
        val coinsTv = view.findViewById<TextView>(R.id.profileCoinsText)
        val xpProgress = view.findViewById<ProgressBar>(R.id.profileXpProgress)

        val coursesCompletedTv = view.findViewById<TextView>(R.id.profileCoursesCompleted)
        val challengesSolvedTv = view.findViewById<TextView>(R.id.profileChallengesSolved)
        val arenaWinsTv = view.findViewById<TextView>(R.id.profileArenaWins)
        val coursesContainer = view.findViewById<LinearLayout>(R.id.profileCoursesContainer)
        val achievementsContainer = view.findViewById<LinearLayout>(R.id.profileAchievementsContainer)

        coursesCompletedTv.text = "0"
        challengesSolvedTv.text = "0"
        arenaWinsTv.text = "0"

        val user = auth.currentUser ?: return

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            doc.getString("username")?.takeIf { it.isNotBlank() }?.let { usernameTv.text = it }

            doc.getString("avatarId")?.let { avatarId ->
                val avatarItem =
                    AvatarRepository.load(requireContext()).firstOrNull { it.id == avatarId }

                avatarItem?.let {
                    val resId = AvatarRepository.resolveDrawableResId(
                        requireContext(), it.drawableName
                    )
                    if (resId != 0) avatarIv.setImageResource(resId)
                }
            }

            val level = doc.getLong("level") ?: 1L
            levelTv.text = "level $level"

            val xp = doc.getLong("xp") ?: 0L
            val xpToNext = doc.getLong("xpToNext") ?: 120L
            val coins = doc.getLong("coins") ?: 0L
            xpTv.text = "XP $xp/$xpToNext"
            coinsTv.text = "$coins coins"

            val max = xpToNext.toInt().coerceAtLeast(1)
            xpProgress.max = max
            xpProgress.progress = xp.toInt().coerceIn(0, max)

            val completedLessons = doc.getLong("lessonNodesCompleted") ?: 0L
            val completedPractices = doc.getLong("practiceNodesCompleted") ?: 0L
            val completedQuizzes = doc.getLong("quizNodesCompleted") ?: 0L
            val completedCode = doc.getLong("codeNodesCompleted") ?: 0L
            coursesCompletedTv.text = completedQuizzes.toString()
            challengesSolvedTv.text = (completedPractices + completedCode).toString()
            arenaWinsTv.text = (doc.getLong("arenaWins") ?: 0L).toString()

            val achievementIds = (doc.get("achievementIds") as? List<*>)
                .orEmpty()
                .mapNotNull { it as? String }
                .toSet()
            val rewardedNodeIds = (doc.get("rewardedNodeIds") as? List<*>)
                .orEmpty()
                .mapNotNull { it as? String }
            renderCompletedCourses(coursesContainer, rewardedNodeIds)
            renderUnlockedAchievements(achievementsContainer, achievementIds)

            val status = doc.getString("onlineStatus") ?: "offline"
            statusDot.setBackgroundResource(
                if (status == "online") R.drawable.bg_status_online
                else R.drawable.bg_status_offline
            )
        }

        btnProfileMenu.setOnClickListener {
            val popup = PopupMenu(view.context, btnProfileMenu)
            popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

            val logoutItem = popup.menu.findItem(R.id.action_logout)
            val redTitle = android.text.SpannableString(logoutItem.title)
            redTitle.setSpan(
                android.text.style.ForegroundColorSpan(
                    resources.getColor(R.color.profile_lo_red, null)
                ), 0, redTitle.length, 0
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

                    R.id.action_reset_progress -> {
                        confirmResetProgress()
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
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(
                R.id.bottom_navigation
            )
            val navHeight = bottomNav.height
            view.setPadding(
                view.paddingLeft, view.paddingTop, view.paddingRight, navHeight
            )
        }

    }

    private fun showAchievement(icon: Int, title: String, desc: String) {
        AchievementBottomSheet.newInstance(icon, title, desc)
            .show(parentFragmentManager, "achievement_bs")
    }

    private fun renderUnlockedAchievements(
        container: LinearLayout,
        achievementIds: Set<String>
    ) {
        container.removeAllViews()
        val unlocked = AchievementCatalog.all.filter { it.id in achievementIds }

        if (unlocked.isEmpty()) {
            container.addView(emptyLabel("No achievements yet"))
            return
        }

        unlocked.forEach { achievement ->
            container.addView(achievementIcon(achievement, unlocked = true))
        }
    }

    private fun renderCompletedCourses(
        container: LinearLayout,
        rewardedNodeIds: List<String>
    ) {
        container.removeAllViews()
        val courses = completedCourseTitles(rewardedNodeIds)

        if (courses.isEmpty()) {
            container.addView(emptyLabel("No completed sections yet"))
            return
        }

        courses.forEach { title ->
            container.addView(courseChip(title))
        }
    }

    private fun completedCourseTitles(rewardedNodeIds: List<String>): List<String> {
        val sections = listOf(
            "java_u1_q1" to "Java Getting Started",
            "java_u2_q1" to "If / Else Statements",
            "java_u3_q1" to "Loops",
            "java_u4_q1" to "Arrays",
            "java_u5_q1" to "Methods",
            "java_u6_q1" to "Scanner Input",
            "java_u7_q1" to "String Tools",
            "java_u8_q1" to "Classes & Objects",
            "java_u9_q1" to "Debugging Basics",
            "java_u10_q1" to "Final Review"
        )
        return sections.filter { (quizNodeId, _) -> quizNodeId in rewardedNodeIds }
            .map { (_, title) -> title }
    }

    private fun achievementIcon(
        achievement: AchievementDefinition,
        unlocked: Boolean
    ): ImageView {
        return ImageView(requireContext()).apply {
            setImageResource(achievement.iconRes)
            alpha = if (unlocked) 1f else 0.28f
            layoutParams = LinearLayout.LayoutParams(dp(64), dp(64)).apply {
                marginEnd = dp(12)
            }
            setOnClickListener {
                showAchievement(
                    achievement.iconRes,
                    achievement.title,
                    "${achievement.description}\nReward: ${achievement.rewardText}"
                )
            }
        }
    }

    private fun courseChip(title: String): TextView {
        return TextView(requireContext()).apply {
            text = title
            setTextColor(resources.getColor(R.color.gc_text_primary, null))
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setBackgroundResource(R.drawable.bg_course_chip)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(10)
            }
        }
    }

    private fun emptyLabel(textValue: String): TextView {
        return TextView(requireContext()).apply {
            text = textValue
            setTextColor(resources.getColor(R.color.gc_text_secondary, null))
            textSize = 13f
            setPadding(dp(2), dp(8), dp(18), dp(8))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun confirmResetProgress() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset progress?")
            .setMessage("This will reset lesson progress, XP, coins, levels, achievements, and learning stats. Your profile details will stay the same.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Reset") { _, _ ->
                resetProgress()
            }
            .show()
    }

    private fun resetProgress() {
        LessonProgressStore.clear(requireContext()) { progressCleared ->
            if (!isAdded) return@clear
            GamificationRepository.resetProgress(requireContext()) { gamificationCleared ->
                if (!isAdded) return@resetProgress
                val success = progressCleared && gamificationCleared
                Toast.makeText(
                    requireContext(),
                    if (success) "Progress reset. You can start fresh." else "Local progress reset. Cloud reset failed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        userListener = null
    }
}
