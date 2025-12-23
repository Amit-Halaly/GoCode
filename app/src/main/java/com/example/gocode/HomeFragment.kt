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
import com.example.gocode.repositories.AvatarRepository
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

    @SuppressLint("CutPasteId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnContinue)
            .setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation
                ).selectedItemId = R.id.learnFragment
            }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStartMission)
            .setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation
                ).selectedItemId = R.id.learnFragment
            }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnArena)
            .setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation
                ).selectedItemId = R.id.arenaFragment
            }

        val arenaRatingTv = view.findViewById<TextView>(R.id.txtArenaRating)


        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        rvNotifications.adapter = NotificationsAdapter(
            listOf(
                "You gained 40 XP yesterday!",
                "Your friend just beat your Arena score!",
                "New daily mission available",
                "Daily streak: 3 days 🔥",
                "You have a new friend!",
                "You are about to level up keep going!"
            )
        )
        rvNotifications.isNestedScrollingEnabled = true

        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        rvCourses.layoutManager = LinearLayoutManager(requireContext())

        val defaultCourses = listOf(
            "Python Basics",
            "Java Fundamentals",
            "C Programming",
            "Algorithms",
            "Variables",
            "Functions"
        )
        rvCourses.adapter = CoursesAdapter(defaultCourses)
        rvCourses.isNestedScrollingEnabled = true


        val userNameTv = view.findViewById<TextView>(R.id.userName)
        val avatarIv = view.findViewById<ImageView>(R.id.avatarImage)
        val userLevelTv = view.findViewById<TextView>(R.id.userLevel)
        val tvXp = view.findViewById<TextView>(R.id.tvXp)
        val xpProgress = view.findViewById<ProgressBar>(R.id.xpProgress)

        val user = auth.currentUser ?: return

        userListener = db.collection("users").document(user.uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            doc.getString("username")?.takeIf { it.isNotBlank() }?.let { userNameTv.text = it }

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

            val rating = doc.getLong("rating") ?: 0L
            arenaRatingTv.text = "Rating: $rating"

            val level = doc.getLong("level") ?: 1L
            userLevelTv.text = "Level $level"

            val xp = doc.getLong("xp") ?: 0L
            val xpToNext = doc.getLong("xpToNext") ?: 1000L

            tvXp.text = "$xp / $xpToNext"
            val max = xpToNext.toInt().coerceAtLeast(1)
            xpProgress.max = max
            xpProgress.progress = xp.toInt().coerceIn(0, max)

            val lang = doc.getString("primaryLanguage")
            val coursesByLang = when (lang) {
                "Python" -> listOf(
                    "Python Basics", "Variables", "Conditions", "Loops", "Functions", "Lists"
                )

                "Java" -> listOf(
                    "Java Fundamentals",
                    "Variables",
                    "OOP Basics",
                    "Classes & Objects",
                    "Methods",
                    "Collections"
                )

                "C" -> listOf(
                    "C Programming", "Pointers", "Arrays", "Functions", "Memory Basics", "Structs"
                )

                else -> defaultCourses
            }
            rvCourses.adapter = CoursesAdapter(coursesByLang)
        }

        val bottomNav =
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_navigation
            )

        val arenaLottie = view.findViewById<LottieAnimationView>(R.id.arenaLottie)

        arenaLottie.setOnClickListener {
            arenaLottie.playAnimation()
        }

        view.post {
            val bottomNav =
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation
                )

            val navHeight = bottomNav.height
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                navHeight
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        userListener = null
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
