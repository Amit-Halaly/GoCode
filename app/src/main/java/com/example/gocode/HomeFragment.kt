package com.example.gocode

import CoursesAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.gocode.lessons.CurriculumRepository
import com.example.gocode.lessons.PathNodeItem
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var userListener: ListenerRegistration? = null
    private var progressListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        view.findViewById<MaterialButton>(R.id.btnContinue).setOnClickListener {
            bottomNav.selectedItemId = R.id.learnFragment
        }
        view.findViewById<MaterialButton>(R.id.btnStartMission).setOnClickListener {
            bottomNav.selectedItemId = R.id.learnFragment
        }
        view.findViewById<MaterialButton>(R.id.btnArena).setOnClickListener {
            bottomNav.selectedItemId = R.id.arenaFragment
        }

        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = NotificationsAdapter(listOf("Loading your progress..."))
            isNestedScrollingEnabled = true
        }

        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CoursesAdapter(listOf("No completed sections yet"))
            isNestedScrollingEnabled = true
        }

        val userNameTv = view.findViewById<TextView>(R.id.userName)
        val avatarIv = view.findViewById<ImageView>(R.id.avatarImage)
        val userLevelTv = view.findViewById<TextView>(R.id.userLevel)
        val tvXp = view.findViewById<TextView>(R.id.tvXp)
        val tvCoins = view.findViewById<TextView>(R.id.tvCoins)
        val xpProgress = view.findViewById<ProgressBar>(R.id.xpProgress)
        val arenaRatingTv = view.findViewById<TextView>(R.id.txtArenaRating)
        val lessonNameTv = view.findViewById<TextView>(R.id.lessonName)
        val lessonMetaTv = view.findViewById<TextView>(R.id.lessonMeta)
        val lessonProgress = view.findViewById<ProgressBar>(R.id.lessonProgress)
        val lessonPercentTv = view.findViewById<TextView>(R.id.lessonPercent)
        val dailyMissionTitle = view.findViewById<TextView>(R.id.tvDailyMissionTitle)
        val dailyMissionBody = view.findViewById<TextView>(R.id.tvDailyMissionBody)
        val dailyMissionXp = view.findViewById<TextView>(R.id.tvDailyMissionXp)
        val dailyMissionCoins = view.findViewById<TextView>(R.id.tvDailyMissionCoins)

        val user = auth.currentUser ?: return

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            doc.getString("username")?.takeIf { it.isNotBlank() }?.let { userNameTv.text = it }

            val avatars = AvatarRepository.load(requireContext())
            val ownedAvatarIds = AvatarRepository.defaultOwnedAvatarIds(avatars) +
                (doc.get("ownedAvatarIds") as? List<*>).orEmpty().mapNotNull { it as? String }
            val requestedAvatarId = doc.getString("avatarId")
            val avatarId = requestedAvatarId
                ?.takeIf { it in ownedAvatarIds }
                ?: AvatarRepository.DEFAULT_AVATAR_ID
            if (requestedAvatarId != avatarId) {
                db.collection("users").document(user.uid).update("avatarId", avatarId)
            }
            val avatarItem = avatars.firstOrNull { it.id == avatarId }
            if (avatarItem != null) {
                val resId = AvatarRepository.resolveDrawableResId(requireContext(), avatarItem.drawableName)
                if (resId != 0) avatarIv.setImageResource(resId)
            }

            val level = doc.getLong("level") ?: 1L
            val xp = doc.getLong("xp") ?: 0L
            val xpToNext = doc.getLong("xpToNext") ?: 120L
            val coins = doc.getLong("coins") ?: 0L
            val rating = doc.getLong("rating") ?: 0L
            val completedPractices = doc.getLong("practiceNodesCompleted") ?: 0L
            val completedQuizzes = doc.getLong("quizNodesCompleted") ?: 0L
            val completedCode = doc.getLong("codeNodesCompleted") ?: 0L
            val rewardedNodeIds = (doc.get("rewardedNodeIds") as? List<*>)
                .orEmpty()
                .mapNotNull { it as? String }

            userLevelTv.text = "Level $level"
            tvXp.text = "$xp / $xpToNext"
            tvCoins.text = "$coins coins"
            arenaRatingTv.text = "Rating: $rating"

            val max = xpToNext.toInt().coerceAtLeast(1)
            xpProgress.max = max
            xpProgress.progress = xp.toInt().coerceIn(0, max)

            rvCourses.adapter = CoursesAdapter(completedCourseTitles(rewardedNodeIds).ifEmpty {
                listOf("No completed sections yet")
            })

            rvNotifications.adapter = NotificationsAdapter(
                buildNotifications(
                    level = level,
                    xp = xp,
                    xpToNext = xpToNext,
                    coins = coins,
                    completedQuizzes = completedQuizzes,
                    completedChallenges = completedPractices + completedCode
                )
            )

            bindDailyMission(
                dailyMissionTitle,
                dailyMissionBody,
                dailyMissionXp,
                dailyMissionCoins,
                completedPractices,
                completedCode
            )
        }

        progressListener = db.collection("users")
            .document(user.uid)
            .collection("nodeProgress")
            .addSnapshotListener { snapshot, _ ->
                val progressByNode = snapshot?.documents.orEmpty().associate { progressDoc ->
                    progressDoc.id to ((progressDoc.getLong("progressPercent") ?: 0L).toInt().coerceIn(0, 100))
                }
                bindContinueLearning(lessonNameTv, lessonMetaTv, lessonProgress, lessonPercentTv, progressByNode)
            }

        val arenaLottie = view.findViewById<LottieAnimationView>(R.id.arenaLottie)
        arenaLottie.setOnClickListener { arenaLottie.playAnimation() }

        view.post {
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomNav.height)
        }
    }

    private fun bindContinueLearning(
        title: TextView,
        meta: TextView,
        progressBar: ProgressBar,
        percent: TextView,
        progressByNode: Map<String, Int>
    ) {
        val nodes = CurriculumRepository.section1("java")
        val next = nodes.firstOrNull { progressByNode.getOrDefault(it.id, 0) < 100 } ?: nodes.last()
        val progress = progressByNode.getOrDefault(next.id, 0).coerceIn(0, 100)

        title.text = next.title
        meta.text = "${nodeTypeLabel(next)} • ${sectionTitle(next.id)}"
        progressBar.progress = progress
        percent.text = "$progress%"
    }

    private fun completedCourseTitles(rewardedNodeIds: List<String>): List<String> {
        return listOf(
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
        ).filter { (quizNodeId, _) -> quizNodeId in rewardedNodeIds }
            .map { (_, title) -> title }
    }

    private fun buildNotifications(
        level: Long,
        xp: Long,
        xpToNext: Long,
        coins: Long,
        completedQuizzes: Long,
        completedChallenges: Long
    ): List<String> {
        val remainingXp = (xpToNext - xp).coerceAtLeast(0L)
        return buildList {
            add("Level $level active • $remainingXp XP to the next level")
            add("$coins coins available for future rewards")
            add("$completedQuizzes Java sections completed")
            add("$completedChallenges practice/code challenges completed")
            if (remainingXp <= 40L) add("You are close to leveling up")
        }
    }

    private fun bindDailyMission(
        title: TextView,
        body: TextView,
        xp: TextView,
        coins: TextView,
        completedPractices: Long,
        completedCode: Long
    ) {
        title.text = "Daily Mission:"
        if (completedCode < completedPractices) {
            body.text = "Complete your next coding challenge"
            xp.text = "+100 XP"
            coins.text = "+75 Coins"
        } else {
            body.text = "Complete your next practice bubble"
            xp.text = "+50 XP"
            coins.text = "+25 Coins"
        }
    }

    private fun nodeTypeLabel(node: PathNodeItem): String {
        return node.type.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun sectionTitle(nodeId: String): String {
        return when {
            nodeId.contains("_u10_") -> "Final Review"
            nodeId.contains("_u9_") -> "Debugging Basics"
            nodeId.contains("_u8_") -> "Classes & Objects"
            nodeId.contains("_u7_") -> "String Tools"
            nodeId.contains("_u6_") -> "Scanner Input"
            nodeId.contains("_u5_") -> "Methods"
            nodeId.contains("_u4_") -> "Arrays"
            nodeId.contains("_u3_") -> "Loops"
            nodeId.contains("_u2_") -> "If / Else Statements"
            else -> "Getting Started"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        progressListener?.remove()
        userListener = null
        progressListener = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = HomeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}
