package com.example.gocode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.Adapters.PathAdapter
import com.example.gocode.models.LessonNode

class JavaPathFragment : Fragment(R.layout.fragment_java_path) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvPath)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val lessons = listOf(
            LessonNode("j1", "Intro to Java", 1, "theory", locked = false, completed = false),
            LessonNode("j2", "Variables & Types", 2, "practice", locked = true, completed = false),
            LessonNode("j3", "Conditions", 3, "practice", locked = true, completed = false),
            LessonNode("j4", "Loops", 4, "practice", locked = true, completed = false),
        )

        rv.adapter = PathAdapter(lessons) { lesson ->
            // TODO: navigate to Lesson screen
        }
    }
}
