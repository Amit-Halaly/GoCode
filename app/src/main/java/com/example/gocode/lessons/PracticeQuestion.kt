package com.example.gocode.lessons

enum class PracticeQuestionType {
    MULTIPLE_CHOICE,
    FILL_BLANK,
    DRAG_FILL_BLANK
}

data class PracticeQuestion(
    val id: String,
    val type: PracticeQuestionType,
    val title: String,
    val question: String,
    val code: String? = null,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val correctAnswers: List<String> = emptyList(),
    val explanation: String
)
