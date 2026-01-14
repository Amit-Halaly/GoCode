package com.example.gocode.lessons

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.gocode.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NodeStartBottomSheet : BottomSheetDialogFragment(R.layout.bottomsheet_node_start) {

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
        btnStartNode.text = "START +$xp XP"

        btnStartNode.isEnabled = !locked
        btnStartNode.alpha = if (locked) 0.4f else 1f

        btnStartNode.setOnClickListener {
            dismiss()
        }
    }

    companion object {

        private const val ARG_NODE_TITLE = "ARG_NODE_TITLE"
        private const val ARG_NODE_TYPE = "ARG_NODE_TYPE"
        private const val ARG_NODE_LOCKED = "ARG_NODE_LOCKED"
        private const val ARG_NODE_XP = "ARG_NODE_XP"

        fun newInstance(node: PathNodeItem): NodeStartBottomSheet {
            return NodeStartBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_NODE_TITLE, node.title)
                    putString(ARG_NODE_TYPE, node.type.name)
                    putBoolean(ARG_NODE_LOCKED, node.locked)
                    putInt(ARG_NODE_XP, 20)
                }
            }
        }
    }
}
