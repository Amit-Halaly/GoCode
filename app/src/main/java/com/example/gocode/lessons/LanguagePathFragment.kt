package com.example.gocode.lessons

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.adapters.PathNodesAdapter

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
        val rvPathNodes = view.findViewById<RecyclerView>(R.id.rvPathNodes)

        tvUnitTop.text = "SECTION 1 • ${language.uppercase()}"
        tvUnitTitle.text = "Getting Started"

        rvPathNodes.itemAnimator = null

        val template = CurriculumRepository.section1(language)
        val completedIds = emptySet<String>() // בהמשך יבוא מ-Firestore
        val nodes = CurriculumRepository.applyProgress(template, completedIds)

        val adapter = PathNodesAdapter(nodes) { node ->
            NodeStartBottomSheet.newInstance(node).show(
                childFragmentManager, "NodeStartBottomSheet"
            )
        }

        rvPathNodes.layoutManager = LinearLayoutManager(requireContext())
        rvPathNodes.adapter = adapter

        rvPathNodes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.kickActivePulse()
                }
            }
        })
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
