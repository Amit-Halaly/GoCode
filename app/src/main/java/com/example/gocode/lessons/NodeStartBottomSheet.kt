package com.example.gocode.lessons

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.gocode.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class NodeStartBottomSheet : BottomSheetDialogFragment(R.layout.bottomsheet_node_start) {

    private var onStartClicked: ((PathNodeItem) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvNodeTitle = view.findViewById<TextView>(R.id.tvNodeTitle)
        val tvNodeSubtitle = view.findViewById<TextView>(R.id.tvNodeSubtitle)
        val btnStartNode = view.findViewById<MaterialButton>(R.id.btnStartNode)

        val title = requireArguments().getString(ARG_NODE_TITLE) ?: "Lesson"
        val type = requireArguments().getString(ARG_NODE_TYPE) ?: "LESSON"
        val locked = requireArguments().getBoolean(ARG_NODE_LOCKED, true)
        val xp = requireArguments().getInt(ARG_NODE_XP, 20)

        tvNodeTitle.text = title
        tvNodeSubtitle.text = type

        if (locked) {
            btnStartNode.isEnabled = false
            btnStartNode.alpha = 0.4f
            btnStartNode.text = "LOCKED 🔒"
        } else {
            btnStartNode.isEnabled = true
            btnStartNode.alpha = 1f
            btnStartNode.text = "START +$xp XP"

            btnStartNode.setOnClickListener {
                dismiss()
                onStartClicked?.invoke(getNodeFromArgs())
            }
        }
    }

    private fun getNodeFromArgs(): PathNodeItem {
        val id = requireArguments().getString(ARG_NODE_ID) ?: "unknown"
        val title = requireArguments().getString(ARG_NODE_TITLE) ?: "Lesson"
        val typeName = requireArguments().getString(ARG_NODE_TYPE) ?: "LESSON"
        val locked = requireArguments().getBoolean(ARG_NODE_LOCKED, true)

        val type = PathNodeType.valueOf(typeName)

        return PathNodeItem(
            id = id, type = type, title = title, offsetDp = 0, progressPercent = 0, locked = locked
        )
    }

    companion object {
        private const val ARG_NODE_ID = "ARG_NODE_ID"
        private const val ARG_NODE_TITLE = "ARG_NODE_TITLE"
        private const val ARG_NODE_TYPE = "ARG_NODE_TYPE"
        private const val ARG_NODE_LOCKED = "ARG_NODE_LOCKED"
        private const val ARG_NODE_XP = "ARG_NODE_XP"

        fun newInstance(
            node: PathNodeItem, xp: Int = 20, onStartClicked: (PathNodeItem) -> Unit
        ): NodeStartBottomSheet {
            return NodeStartBottomSheet().apply {
                this.onStartClicked = onStartClicked
                arguments = Bundle().apply {
                    putString(ARG_NODE_ID, node.id)
                    putString(ARG_NODE_TITLE, node.title)
                    putString(ARG_NODE_TYPE, node.type.name)
                    putBoolean(ARG_NODE_LOCKED, node.locked)
                    putInt(ARG_NODE_XP, xp)
                }
            }
        }
    }
}
