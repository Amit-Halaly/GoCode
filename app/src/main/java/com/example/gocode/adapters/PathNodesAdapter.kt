package com.example.gocode.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.gocode.R
import com.example.gocode.lessons.PathNodeItem
import com.example.gocode.lessons.PathNodeType
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

class PathNodesAdapter(
    private var items: List<PathNodeItem>,
    private val onNodeClick: (PathNodeItem) -> Unit
) : RecyclerView.Adapter<PathNodesAdapter.NodeVH>() {

    private val activeIndex: Int
        get() = items.indexOfFirst { !it.locked && it.progressPercent < 100 }.let { idx ->
            if (idx == -1) {
                items.indexOfFirst { !it.locked }.let { if (it == -1) 0 else it }
            } else {
                idx
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_path_node, parent, false)
        return NodeVH(view)
    }

    override fun onBindViewHolder(holder: NodeVH, position: Int) {
        val item = items[position]
        val isActive = position == activeIndex && !item.locked
        holder.bind(item = item, isActive = isActive, onNodeClick = onNodeClick)
    }

    override fun onViewAttachedToWindow(holder: NodeVH) {
        super.onViewAttachedToWindow(holder)

        val position = holder.bindingAdapterPosition
        if (position == RecyclerView.NO_POSITION) return

        val item = items[position]
        holder.applyPulse(position == activeIndex && !item.locked)
    }

    fun kickActivePulse() {
        if (activeIndex in items.indices) {
            notifyItemChanged(activeIndex)
        }
    }

    fun submitItems(nextItems: List<PathNodeItem>) {
        items = nextItems
        notifyDataSetChanged()
    }

    fun itemAt(position: Int): PathNodeItem? {
        return items.getOrNull(position)
    }

    override fun getItemCount(): Int = items.size

    class NodeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nodeWrapper: View = itemView.findViewById(R.id.nodeWrapper)
        private val cardSectionHeader: MaterialCardView = itemView.findViewById(R.id.cardSectionHeader)
        private val tvSectionTop: TextView = itemView.findViewById(R.id.tvSectionTop)
        private val tvSectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val cardNode: MaterialCardView = itemView.findViewById(R.id.cardNode)
        private val progressRing: CircularProgressIndicator =
            itemView.findViewById(R.id.progressRing)
        private val lottieNodeIcon: LottieAnimationView = itemView.findViewById(R.id.lottieNodeIcon)

        private var lastLottieRes: Int? = null

        fun bind(
            item: PathNodeItem,
            isActive: Boolean,
            onNodeClick: (PathNodeItem) -> Unit
        ) {
            val lottieRes = when (item.type) {
                PathNodeType.LESSON -> R.raw.lesson
                PathNodeType.PRACTICE -> R.raw.practice
                PathNodeType.QUIZ -> R.raw.quiz
                PathNodeType.CODE -> R.raw.code
            }

            if (lastLottieRes != lottieRes) {
                lastLottieRes = lottieRes
                lottieNodeIcon.cancelAnimation()
                lottieNodeIcon.setAnimation(lottieRes)
                lottieNodeIcon.progress = 0f
            }

            if (!lottieNodeIcon.isAnimating) {
                lottieNodeIcon.playAnimation()
            }

            val sectionColor = sectionColor(item)
            bindSectionHeader(item, sectionColor)
            cardNode.setCardBackgroundColor(sectionColor)
            progressRing.setIndicatorColor(sectionColor)
            progressRing.progress = item.progressPercent.coerceIn(0, 100)

            if (item.locked) {
                cardNode.alpha = 0.35f
                progressRing.alpha = 0.18f
                lottieNodeIcon.alpha = 0.55f
            } else {
                cardNode.alpha = 1f
                progressRing.alpha = 1f
                lottieNodeIcon.alpha = 1f
            }

            val layoutParams = nodeWrapper.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = dp(itemView, item.offsetDp)
            layoutParams.rightMargin = 0
            nodeWrapper.layoutParams = layoutParams

            applyPulse(isActive)

            cardNode.setOnClickListener { onNodeClick(item) }
            nodeWrapper.setOnClickListener { onNodeClick(item) }
        }

        fun applyPulse(isActive: Boolean) {
            nodeWrapper.clearAnimation()

            if (isActive) {
                nodeWrapper.startAnimation(
                    AnimationUtils.loadAnimation(itemView.context, R.anim.pulse)
                )
                progressRing.scaleX = 1.06f
                progressRing.scaleY = 1.06f
            } else {
                progressRing.scaleX = 1f
                progressRing.scaleY = 1f
            }
        }

        private fun sectionColor(item: PathNodeItem): Int {
            val colorRes = when {
                item.id.contains("_u10_") -> R.color.section_ten
                item.id.contains("_u9_") -> R.color.section_nine
                item.id.contains("_u8_") -> R.color.section_eight
                item.id.contains("_u7_") -> R.color.section_seven
                item.id.contains("_u6_") -> R.color.section_six
                item.id.contains("_u5_") -> R.color.section_five
                item.id.contains("_u4_") -> R.color.section_four
                item.id.contains("_u2_") -> R.color.section_two
                item.id.contains("_u3_") -> R.color.section_three
                else -> R.color.section_one
            }
            return ContextCompat.getColor(itemView.context, colorRes)
        }

        private fun bindSectionHeader(item: PathNodeItem, sectionColor: Int) {
            val header = when (item.id) {
                "java_u2_l1" -> "SECTION 2 • JAVA" to "If / Else Statements"
                "java_u3_l1" -> "SECTION 3 • JAVA" to "Loops"
                "java_u4_l1" -> "SECTION 4 • JAVA" to "Arrays"
                "java_u5_l1" -> "SECTION 5 • JAVA" to "Methods"
                "java_u6_l1" -> "SECTION 6 • JAVA" to "Scanner Input"
                "java_u7_l1" -> "SECTION 7 • JAVA" to "String Tools"
                "java_u8_l1" -> "SECTION 8 • JAVA" to "Classes & Objects"
                "java_u9_l1" -> "SECTION 9 • JAVA" to "Debugging Basics"
                "java_u10_l1" -> "SECTION 10 • JAVA" to "Final Review"
                "py_u2_l1" -> "SECTION 2 - PYTHON" to "If / Else Statements"
                "py_u3_l1" -> "SECTION 3 - PYTHON" to "Loops"
                "py_u4_l1" -> "SECTION 4 - PYTHON" to "Lists"
                "py_u5_l1" -> "SECTION 5 - PYTHON" to "Functions"
                "py_u6_l1" -> "SECTION 6 - PYTHON" to "Input"
                "py_u7_l1" -> "SECTION 7 - PYTHON" to "String Tools"
                "py_u8_l1" -> "SECTION 8 - PYTHON" to "Dictionaries"
                "py_u9_l1" -> "SECTION 9 - PYTHON" to "Debugging Basics"
                "py_u10_l1" -> "SECTION 10 - PYTHON" to "Final Review"
                else -> null
            }

            if (header == null) {
                cardSectionHeader.visibility = View.GONE
                return
            }

            cardSectionHeader.visibility = View.VISIBLE
            cardSectionHeader.setCardBackgroundColor(sectionColor)
            tvSectionTop.text = header.first
            tvSectionTitle.text = header.second
        }

        private fun dp(view: View, value: Int): Int {
            return (value * view.resources.displayMetrics.density).toInt()
        }
    }
}
