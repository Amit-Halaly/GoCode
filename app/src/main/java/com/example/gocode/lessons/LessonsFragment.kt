package com.example.gocode.lessons

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gocode.R
import com.google.android.material.button.MaterialButton

class LessonsFragment : Fragment(R.layout.fragment_lessons) {

    private lateinit var btnPickLanguage: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPickLanguage = view.findViewById(R.id.btnPickLanguage)

        if (savedInstanceState == null) {
            showLanguagePath("java")
        }

        btnPickLanguage.setOnClickListener {
            LanguagePickerBottomSheet { lang ->
                showLanguagePath(lang)
            }.show(childFragmentManager, "LanguagePicker")
        }
    }

    private fun showLanguagePath(lang: String) {
        btnPickLanguage.text = languageName(lang)

        val fragment = LanguagePathFragment.newInstance(lang).apply {
            onSectionColorChanged = ::setLanguageButtonColor
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.lessonsContainer, fragment)
            .commit()

        val fallbackColor = if (lang == "c") {
            R.color.section_nine
        } else if (lang == "cpp") {
            R.color.section_eight
        } else {
            R.color.section_one
        }
        setLanguageButtonColor(ContextCompat.getColor(requireContext(), fallbackColor))
    }

    private fun setLanguageButtonColor(color: Int) {
        btnPickLanguage.backgroundTintList = ColorStateList.valueOf(color)
        btnPickLanguage.iconTint = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.gc_text_primary)
        )
    }

    private fun languageName(lang: String): String {
        return when (lang) {
            "python" -> "Python"
            "java" -> "Java"
            "c" -> "C"
            "cpp" -> "C++"
            else -> "Java"
        }
    }
}
