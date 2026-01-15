package com.example.gocode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AchievementInfoBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_achievement, container, false)

        val icon = view.findViewById<ImageView>(R.id.bsIcon)
        val title = view.findViewById<TextView>(R.id.bsTitle)
        val description = view.findViewById<TextView>(R.id.bsDescription)

        icon.setImageResource(requireArguments().getInt(ARG_ICON))
        title.text = requireArguments().getString(ARG_TITLE)
        description.text = requireArguments().getString(ARG_DESC)

        return view
    }

    companion object {
        private const val ARG_ICON = "icon"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"

        fun newInstance(icon: Int, title: String, desc: String): AchievementInfoBottomSheet {
            return AchievementInfoBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ICON, icon)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, desc)
                }
            }
        }
    }
}
