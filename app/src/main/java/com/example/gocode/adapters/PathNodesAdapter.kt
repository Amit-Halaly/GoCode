package com.example.gocode.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.gocode.R
import com.example.gocode.lessons.PathNodeItem
import com.example.gocode.lessons.PathNodeType
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

class PathNodesAdapter(
    private val items: List<PathNodeItem>, private val onNodeClick: (PathNodeItem) -> Unit
) : RecyclerView.Adapter<PathNodesAdapter.NodeVH>() {

    private val activeIndex: Int = items.indexOfFirst { !it.locked }.let { idx ->
        if (idx == -1) 0 else idx
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_path_node, parent, false)
        return NodeVH(v)
    }

    override fun onBindViewHolder(holder: NodeVH, position: Int) {
        val item = items[position]
        val lockedNow = item.locked
        val isActive = (position == activeIndex) && !lockedNow

        holder.bind(item = item, isActive = isActive, onNodeClick = onNodeClick)
    }


    override fun onViewAttachedToWindow(holder: NodeVH) {
        super.onViewAttachedToWindow(holder)

        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) return

        val item = items[pos]
        val isActive = (pos == activeIndex) && !item.locked
        holder.applyPulse(isActive)
    }


    fun kickActivePulse() {
        if (activeIndex in items.indices) {
            notifyItemChanged(activeIndex)
        }
    }

    override fun getItemCount(): Int = items.size

    class NodeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nodeWrapper: View = itemView.findViewById(R.id.nodeWrapper)
        private val cardNode: MaterialCardView = itemView.findViewById(R.id.cardNode)
        private val progressRing: CircularProgressIndicator =
            itemView.findViewById(R.id.progressRing)
        private val lottieNodeIcon: LottieAnimationView = itemView.findViewById(R.id.lottieNodeIcon)

        private var lastLottieRes: Int? = null

        fun bind(
            item: PathNodeItem, isActive: Boolean, onNodeClick: (PathNodeItem) -> Unit
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

            val lp = nodeWrapper.layoutParams as ViewGroup.MarginLayoutParams
            lp.leftMargin = dp(itemView, item.offsetDp)
            lp.rightMargin = 0
            nodeWrapper.layoutParams = lp
            applyPulse(isActive)
            cardNode.setOnClickListener {
                onNodeClick(item)
            }
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

        private fun dp(view: View, value: Int): Int {
            return (value * view.resources.displayMetrics.density).toInt()
        }
    }
}
