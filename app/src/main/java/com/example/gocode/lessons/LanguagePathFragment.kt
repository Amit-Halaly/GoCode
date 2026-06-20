package com.example.gocode.lessons

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.ExerciseRunActivity
import com.example.gocode.R
import com.example.gocode.adapters.PathNodesAdapter
import com.google.android.material.card.MaterialCardView

class LanguagePathFragment : Fragment(R.layout.fragment_language_path) {

    private var language: String = "python"
    private var pathAdapter: PathNodesAdapter? = null
    private lateinit var cardUnitHeader: MaterialCardView
    private lateinit var tvUnitTop: TextView
    private lateinit var tvUnitTitle: TextView
    private var currentSectionNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = arguments?.getString(ARG_LANGUAGE) ?: "python"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardUnitHeader = view.findViewById(R.id.cardUnitHeader)
        tvUnitTop = view.findViewById(R.id.tvUnitTop)
        tvUnitTitle = view.findViewById(R.id.tvUnitTitle)
        val rvPathNodes = view.findViewById<RecyclerView>(R.id.rvPathNodes)

        updateSectionHeader(1)
        rvPathNodes.itemAnimator = null

        val template = CurriculumRepository.section1(language)
        val nodes = loadNodes(template)

        val adapter = PathNodesAdapter(nodes) { node ->
            NodeStartBottomSheet.newInstance(node, xp = 20) { clickedNode ->
                when (clickedNode.type) {
                    PathNodeType.LESSON -> {
                        startActivity(
                            Intent(requireContext(), LessonFlowActivity::class.java)
                                .putExtra(EXTRA_NODE_ID, clickedNode.id)
                        )
                    }

                    PathNodeType.PRACTICE -> {
                        startActivity(
                            Intent(requireContext(), PracticeFlowActivity::class.java)
                                .putExtra(EXTRA_NODE_ID, clickedNode.id)
                        )
                    }

                    PathNodeType.QUIZ -> {
                        startActivity(
                            Intent(requireContext(), PracticeFlowActivity::class.java)
                                .putExtra(EXTRA_NODE_ID, clickedNode.id)
                        )
                    }

                    PathNodeType.CODE -> {
                        startActivity(
                            Intent(requireContext(), ExerciseRunActivity::class.java)
                                .putExtra(EXTRA_NODE_ID, clickedNode.id)
                        )
                    }
                }
            }.show(childFragmentManager, "NodeStartBottomSheet")
        }

        pathAdapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        rvPathNodes.layoutManager = layoutManager
        rvPathNodes.adapter = adapter

        rvPathNodes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val visibleNode = adapter.itemAt(firstVisiblePosition) ?: return
                updateSectionHeader(sectionNumberForNode(visibleNode))
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.kickActivePulse()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val template = CurriculumRepository.section1(language)
        pathAdapter?.submitItems(loadNodes(template))
    }

    private fun loadNodes(template: List<PathNodeItem>): List<PathNodeItem> {
        val progress = LessonProgressStore.getProgressMap(
            requireContext(),
            template.map { it.id }
        )

        return CurriculumRepository.applyProgress(template, progress)
            .map { it.copy(locked = false) }
    }

    private fun updateSectionHeader(sectionNumber: Int) {
        if (currentSectionNumber == sectionNumber) return

        currentSectionNumber = sectionNumber
        val section = sectionInfo(sectionNumber)

        tvUnitTop.text = "SECTION ${section.number} • ${language.uppercase()}"
        tvUnitTitle.text = section.title
        cardUnitHeader.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), section.colorRes)
        )
    }

    private fun sectionNumberForNode(node: PathNodeItem): Int {
        return when {
            node.id.contains("_u2_") -> 2
            node.id.contains("_u3_") -> 3
            else -> 1
        }
    }

    private fun sectionInfo(sectionNumber: Int): SectionInfo {
        return when (sectionNumber) {
            2 -> SectionInfo(2, "If / Else Statements", R.color.section_two)
            3 -> SectionInfo(3, "Loops", R.color.section_three)
            else -> SectionInfo(1, "Getting Started", R.color.section_one)
        }
    }

    private data class SectionInfo(
        val number: Int,
        val title: String,
        val colorRes: Int
    )

    companion object {
        private const val ARG_LANGUAGE = "ARG_LANGUAGE"
        const val EXTRA_NODE_ID = "EXTRA_NODE_ID"

        fun newInstance(language: String): LanguagePathFragment {
            return LanguagePathFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LANGUAGE, language)
                }
            }
        }
    }
}
