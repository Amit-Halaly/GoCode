package com.example.gocode.lessons

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.adapters.PathNodesAdapter
import com.google.android.material.snackbar.Snackbar

class LanguagePathFragment : Fragment(R.layout.fragment_language_path) {

    private var language: String = "python"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = arguments?.getString(ARG_LANGUAGE) ?: "python"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUnitTop = view.findViewById<TextView>(R.id.tvUnitTop)
        val tvUnitTitle = view.findViewById<TextView>(R.id.tvUnitTitle)

        tvUnitTop.text = "SECTION 1 • ${language.uppercase()}"
        tvUnitTitle.text = "Getting Started"

        val rvPathNodes = view.findViewById<RecyclerView>(R.id.rvPathNodes)

        val nodes = listOf(
            PathNodeItem("n1", "lesson", "1", locked = false, offsetDp = 0),
            PathNodeItem("n2", "practice", "⭐", locked = false, offsetDp = 40),
            PathNodeItem("n3", "lesson", "2", locked = false, offsetDp = 10),
            PathNodeItem("n4", "practice", "🎯", locked = false, offsetDp = 60),
            PathNodeItem("n5", "lesson", "3", locked = true, offsetDp = 20),
            PathNodeItem("n6", "quiz", "🏁", locked = true, offsetDp = 70),
            PathNodeItem("n7", "code", "💻", locked = true, offsetDp = 30)
        )

        rvPathNodes.adapter = PathNodesAdapter(nodes) { node ->
            Snackbar.make(view, "Clicked: ${node.type} (${node.id})", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_LANGUAGE = "ARG_LANGUAGE"

        fun newInstance(language: String): LanguagePathFragment {
            return LanguagePathFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LANGUAGE, language)
                }
            }
        }
    }
}
