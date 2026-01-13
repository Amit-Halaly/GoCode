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
        rvPathNodes.itemAnimator = null
        val template = CurriculumRepository.section1(language)
        val completedIds = setOf<String>(

        )

        val nodes = CurriculumRepository.applyProgress(template, completedIds)


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
