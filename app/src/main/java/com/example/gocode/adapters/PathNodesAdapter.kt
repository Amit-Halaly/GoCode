package com.example.gocode.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.gocode.R
import com.example.gocode.lessons.PathNodeItem
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

class PathNodesAdapter(
    private val items: List<PathNodeItem>, private val onNodeClick: (PathNodeItem) -> Unit
) : RecyclerView.Adapter<PathNodesAdapter.NodeVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_path_node, parent, false)
        return NodeVH(v)
    }

    override fun onBindViewHolder(holder: NodeVH, position: Int) {
        holder.bind(items[position], onNodeClick)
    }

    override fun getItemCount(): Int = items.size

    class NodeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nodeWrapper: View = itemView.findViewById(R.id.nodeWrapper)
        private val cardNode: MaterialCardView = itemView.findViewById(R.id.cardNode)

        private val progressRing: CircularProgressIndicator =
            itemView.findViewById(R.id.progressRing)

        private val lottieNodeIcon: LottieAnimationView = itemView.findViewById(R.id.lottieNodeIcon)

        fun bind(item: PathNodeItem, onNodeClick: (PathNodeItem) -> Unit) {

            val lottieRes = when (item.type) {
                "lesson" -> R.raw.lesson
                "practice" -> R.raw.practice
                "quiz" -> R.raw.quiz
                "code" -> R.raw.code
                else -> R.raw.lesson
            }

            lottieNodeIcon.cancelAnimation()
            lottieNodeIcon.setAnimation(lottieRes)
            lottieNodeIcon.progress = 0f
            lottieNodeIcon.playAnimation()

            progressRing.progress = getProgressPercentSafe(item)

            if (item.locked) {
                cardNode.alpha = 0.35f
                progressRing.alpha = 0.25f
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

            cardNode.setOnClickListener {
                if (!item.locked) onNodeClick(item)
            }
        }

        private fun getProgressPercentSafe(item: PathNodeItem): Int {
            return when (item.type) {
                "lesson" -> 25
                "practice" -> 60
                "quiz" -> 85
                "code" -> 100
                else -> 20
            }
        }

        private fun dp(view: View, value: Int): Int {
            return (value * view.resources.displayMetrics.density).toInt()
        }
    }
}
