package com.example.gocode.lessons

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gocode.R
import com.google.android.material.button.MaterialButton

class LessonsFragment : Fragment(R.layout.fragment_lessons) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPickLanguage = view.findViewById<MaterialButton>(R.id.btnPickLanguage)

        btnPickLanguage.setOnClickListener {
            LanguagePickerBottomSheet { lang ->

                val name = when (lang) {
                    "python" -> "Python"
                    "java" -> "Java"
                    "c" -> "C"
                    else -> "Python"
                }

                btnPickLanguage.text = name

                childFragmentManager.beginTransaction()
                    .replace(R.id.lessonsContainer, LanguagePathFragment.newInstance(lang))
                    .commit()

            }.show(childFragmentManager, "LanguagePicker")
        }
    }
}
