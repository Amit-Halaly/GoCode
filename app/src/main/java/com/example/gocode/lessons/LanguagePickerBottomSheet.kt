package com.example.gocode.lessons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gocode.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class LanguagePickerBottomSheet(
    private val onPicked: (String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_language_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialCardView>(R.id.cardPython).setOnClickListener {
            onPicked("python"); dismiss()
        }
        view.findViewById<MaterialCardView>(R.id.cardJava).setOnClickListener {
            onPicked("java"); dismiss()
        }
        view.findViewById<MaterialCardView>(R.id.cardC).setOnClickListener {
            onPicked("c"); dismiss()
        }
    }
}
