package com.example.gocode.lessons

import android.annotation.SuppressLint
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
import com.example.gocode.firebase.FirebaseContentRepository
import com.example.gocode.gamification.GamificationRepository
import com.google.android.material.card.MaterialCardView

class LanguagePathFragment : Fragment(R.layout.fragment_language_path) {

    private var language: String = "python"
    private var pathAdapter: PathNodesAdapter? = null
    private lateinit var cardUnitHeader: MaterialCardView
    private lateinit var tvUnitTop: TextView
    private lateinit var tvUnitTitle: TextView
    private lateinit var cardComingSoon: MaterialCardView
    private lateinit var tvComingSoonTitle: TextView
    private lateinit var tvComingSoonBody: TextView
    private var currentSectionNumber = 0
    var onSectionColorChanged: ((Int) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = arguments?.getString(ARG_LANGUAGE) ?: "python"
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardUnitHeader = view.findViewById(R.id.cardUnitHeader)
        tvUnitTop = view.findViewById(R.id.tvUnitTop)
        tvUnitTitle = view.findViewById(R.id.tvUnitTitle)
        cardComingSoon = view.findViewById(R.id.cardComingSoon)
        tvComingSoonTitle = view.findViewById(R.id.tvComingSoonTitle)
        tvComingSoonBody = view.findViewById(R.id.tvComingSoonBody)
        val rvPathNodes = view.findViewById<RecyclerView>(R.id.rvPathNodes)

        updateSectionHeader(1)
        rvPathNodes.itemAnimator = null

        if (language.lowercase() != "java") {
            showComingSoon(rvPathNodes)
            return
        }

        val template = CurriculumRepository.section1(language)
        val nodes = loadNodes(template)

        val adapter = PathNodesAdapter(nodes) { node ->
            NodeStartBottomSheet.newInstance(node, xp = GamificationRepository.rewardForNode(node.id).xp.toInt()) { clickedNode ->
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
        refreshNodesFromFirebase(template)
        refreshPathFromFirebase()

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
        if (language.lowercase() != "java") return
        val template = CurriculumRepository.section1(language)
        pathAdapter?.submitItems(loadNodes(template))
        refreshNodesFromFirebase(template)
        refreshPathFromFirebase()
    }

    private fun loadNodes(template: List<PathNodeItem>): List<PathNodeItem> {
        val progress = LessonProgressStore.getProgressMap(
            requireContext(),
            template.map { it.id }
        )

        return CurriculumRepository.applyProgress(template, progress)
            .map { it.copy(locked = false) }
    }

    private fun refreshNodesFromFirebase(template: List<PathNodeItem>) {
        val context = context ?: return
        LessonProgressStore.getProgressMap(context, template.map { it.id }) { progress ->
            if (!isAdded) return@getProgressMap
            pathAdapter?.submitItems(
                CurriculumRepository.applyProgress(template, progress)
                    .map { it.copy(locked = false) }
            )
        }
    }

    private fun refreshPathFromFirebase() {
        FirebaseContentRepository.getPathNodes { remoteTemplate ->
            if (!isAdded || remoteTemplate.isEmpty()) return@getPathNodes
            pathAdapter?.submitItems(loadNodes(remoteTemplate))
            refreshNodesFromFirebase(remoteTemplate)
        }
    }

    private fun updateSectionHeader(sectionNumber: Int) {
        if (currentSectionNumber == sectionNumber) return

        currentSectionNumber = sectionNumber
        val section = sectionInfo(sectionNumber)
        val sectionColor = ContextCompat.getColor(requireContext(), section.colorRes)

        tvUnitTop.text = "SECTION ${section.number} • ${language.uppercase()}"
        tvUnitTitle.text = section.title
        cardUnitHeader.setCardBackgroundColor(sectionColor)
        onSectionColorChanged?.invoke(sectionColor)
    }

    private fun showComingSoon(rvPathNodes: RecyclerView) {
        rvPathNodes.visibility = View.GONE
        cardComingSoon.visibility = View.VISIBLE

        val displayName = when (language.lowercase()) {
            "python" -> "Python"
            "c" -> "C"
            else -> language.replaceFirstChar { it.uppercase() }
        }

        tvUnitTop.text = "${displayName.uppercase()} PATH"
        tvUnitTitle.text = "Coming Soon"
        tvComingSoonTitle.text = "$displayName lessons are coming soon"
        tvComingSoonBody.text =
            "We are preparing a polished $displayName learning path for a future update. For now, the full guided course is available in Java."
        val comingSoonColor = ContextCompat.getColor(requireContext(), R.color.section_nine)
        cardUnitHeader.setCardBackgroundColor(comingSoonColor)
        onSectionColorChanged?.invoke(comingSoonColor)
    }

    private fun sectionNumberForNode(node: PathNodeItem): Int {
        return when {
            node.id.contains("_u10_") -> 10
            node.id.contains("_u9_") -> 9
            node.id.contains("_u8_") -> 8
            node.id.contains("_u7_") -> 7
            node.id.contains("_u6_") -> 6
            node.id.contains("_u5_") -> 5
            node.id.contains("_u4_") -> 4
            node.id.contains("_u2_") -> 2
            node.id.contains("_u3_") -> 3
            else -> 1
        }
    }

    private fun sectionInfo(sectionNumber: Int): SectionInfo {
        return when (sectionNumber) {
            2 -> SectionInfo(2, "If / Else Statements", R.color.section_two)
            3 -> SectionInfo(3, "Loops", R.color.section_three)
            4 -> SectionInfo(4, "Arrays", R.color.section_four)
            5 -> SectionInfo(5, "Methods", R.color.section_five)
            6 -> SectionInfo(6, "Scanner Input", R.color.section_six)
            7 -> SectionInfo(7, "String Tools", R.color.section_seven)
            8 -> SectionInfo(8, "Classes & Objects", R.color.section_eight)
            9 -> SectionInfo(9, "Debugging Basics", R.color.section_nine)
            10 -> SectionInfo(10, "Final Review", R.color.section_ten)
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
