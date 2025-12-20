package com.example.gocode

import CoursesAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContinue = view.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnContinue
        )
        btnContinue.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_navigation
            ).selectedItemId = R.id.learnFragment
        }

        val btnStartMission = view.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnStartMission
        )

        btnStartMission.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_navigation
            ).selectedItemId = R.id.learnFragment
        }

        val btnArena = view.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnArena
        )

        btnArena.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_navigation
            ).selectedItemId = R.id.arenaFragment
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvNotifications)

        val demoNotifications = listOf(
            "You gained 40 XP yesterday!",
            "Your friend just beat your Arena score!",
            "New daily mission available",
            "You reached Level 6 🎉",
            "Arena match reward is waiting",
            "Daily streak: 3 days 🔥"
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = NotificationsAdapter(demoNotifications)

        val courses = listOf(
            "Python Basics",
            "Java Fundamentals",
            "C Programming",
            "Algorithms",
            "Variables",
            "Functions"
        )

        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)

        rvCourses.layoutManager = LinearLayoutManager(requireContext())
        rvCourses.adapter = CoursesAdapter(courses)

        val bottomNav =
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_navigation
            )

        view.setOnApplyWindowInsetsListener { v, insets ->
            val navHeight = bottomNav.height
            v.setPadding(
                v.paddingLeft, v.paddingTop, v.paddingRight, navHeight
            )
            insets
        }


    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = HomeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}