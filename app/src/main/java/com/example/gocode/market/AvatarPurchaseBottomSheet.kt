package com.example.gocode.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.gocode.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AvatarPurchaseBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_avatar_purchase, container, false)

        view.findViewById<ImageView>(R.id.purchaseAvatarImage)
            .setImageResource(requireArguments().getInt(ARG_IMAGE_RES))
        view.findViewById<TextView>(R.id.purchaseTitle).text =
            requireArguments().getString(ARG_TITLE)
        view.findViewById<TextView>(R.id.purchaseDescription).text =
            requireArguments().getString(ARG_DESCRIPTION)
        view.findViewById<TextView>(R.id.purchaseRarity).text =
            requireArguments().getString(ARG_RARITY)
        view.findViewById<TextView>(R.id.purchasePrice).text =
            requireArguments().getString(ARG_PRICE)
        view.findViewById<MaterialButton>(R.id.purchaseContinue)
            .setOnClickListener { dismiss() }

        return view
    }

    companion object {
        private const val ARG_IMAGE_RES = "imageRes"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_RARITY = "rarity"
        private const val ARG_PRICE = "price"

        fun newInstance(
            imageRes: Int,
            avatarName: String,
            rarity: String,
            price: Long
        ): AvatarPurchaseBottomSheet {
            return AvatarPurchaseBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_IMAGE_RES, imageRes)
                    putString(ARG_TITLE, "$avatarName unlocked")
                    putString(ARG_DESCRIPTION, "Added to your collection and equipped for your next lesson.")
                    putString(ARG_RARITY, rarity)
                    putString(ARG_PRICE, "$price coins spent")
                }
            }
        }
    }
}
