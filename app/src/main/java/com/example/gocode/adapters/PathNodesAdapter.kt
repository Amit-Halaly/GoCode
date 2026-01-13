package com.example.gocode.adapters

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        holder.bind(
            item = items[position],
            isActive = position == activeIndex && !items[position].locked,
            onNodeClick = onNodeClick
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: NodeVH) {
        super.onViewRecycled(holder)
        holder.stopAnimations()
    }

    class NodeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nodeWrapper: View = itemView.findViewById(R.id.nodeWrapper)
        private val cardNode: MaterialCardView = itemView.findViewById(R.id.cardNode)
        private val progressRing: CircularProgressIndicator =
            itemView.findViewById(R.id.progressRing)
        private val lottieNodeIcon: LottieAnimationView = itemView.findViewById(R.id.lottieNodeIcon)

        private var pulseAnimX: ObjectAnimator? = null
        private var pulseAnimY: ObjectAnimator? = null

        fun bind(
            item: PathNodeItem, isActive: Boolean, onNodeClick: (PathNodeItem) -> Unit
        ) {
            val lockedNow = item.locked

            val lottieRes = when (item.type) {
                PathNodeType.LESSON -> R.raw.lesson
                PathNodeType.PRACTICE -> R.raw.practice
                PathNodeType.QUIZ -> R.raw.quiz
                PathNodeType.CODE -> R.raw.code
            }

            lottieNodeIcon.cancelAnimation()
            lottieNodeIcon.setAnimation(lottieRes)
            lottieNodeIcon.progress = 0f
            lottieNodeIcon.playAnimation()

            progressRing.progress = item.progressPercent.coerceIn(0, 100)

            if (lockedNow) {
                cardNode.alpha = 0.35f
                progressRing.alpha = 0.18f
                lottieNodeIcon.alpha = 0.35f
                cardNode.isClickable = false
            } else {
                cardNode.alpha = 1f
                progressRing.alpha = 1f
                lottieNodeIcon.alpha = 1f
                cardNode.isClickable = true
            }

            val lp = nodeWrapper.layoutParams as ViewGroup.MarginLayoutParams
            lp.leftMargin = dp(itemView, item.offsetDp)
            lp.rightMargin = 0
            nodeWrapper.layoutParams = lp

            if (isActive && !lockedNow) {
                startPulse()
            } else {
                stopPulse()
            }

            cardNode.setOnClickListener {
                if (!lockedNow) onNodeClick(item)
            }
        }

        private fun startPulse() {
            if (pulseAnimX?.isRunning == true && pulseAnimY?.isRunning == true) return

            stopPulse()

            pulseAnimX = ObjectAnimator.ofFloat(nodeWrapper, View.SCALE_X, 1f, 1.08f).apply {
                duration = 550
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }

            pulseAnimY = ObjectAnimator.ofFloat(nodeWrapper, View.SCALE_Y, 1f, 1.08f).apply {
                duration = 550
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }

            pulseAnimX?.start()
            pulseAnimY?.start()
        }

        private fun stopPulse() {
            pulseAnimX?.cancel()
            pulseAnimY?.cancel()
            pulseAnimX = null
            pulseAnimY = null

            nodeWrapper.scaleX = 1f
            nodeWrapper.scaleY = 1f
        }

        fun stopAnimations() {
            stopPulse()
            lottieNodeIcon.cancelAnimation()
        }

        private fun dp(view: View, value: Int): Int {
            return (value * view.resources.displayMetrics.density).toInt()
        }
    }
}
