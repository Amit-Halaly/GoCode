package com.example.gocode.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.models.LessonNode

class PathAdapter(
    private val items: List<LessonNode>,
    private val onClick: (LessonNode) -> Unit
) : RecyclerView.Adapter<PathAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_path_node, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position, onClick)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val left = itemView.findViewById<View>(R.id.leftContainer)
        private val right = itemView.findViewById<View>(R.id.rightContainer)

        private val tvLeft = itemView.findViewById<TextView>(R.id.tvTitleLeft)
        private val tvRight = itemView.findViewById<TextView>(R.id.tvTitleRight)


        fun bind(node: LessonNode, position: Int, onClick: (LessonNode) -> Unit) {
            val isLeft = position % 2 == 0

            left.visibility = if (isLeft) View.VISIBLE else View.GONE
            right.visibility = if (isLeft) View.GONE else View.VISIBLE

            if (isLeft) tvLeft.text = node.title else tvRight.text = node.title

            itemView.alpha = if (node.locked) 0.5f else 1f
            itemView.isClickable = !node.locked
            itemView.setOnClickListener { if (!node.locked) onClick(node) }
        }
    }
}
