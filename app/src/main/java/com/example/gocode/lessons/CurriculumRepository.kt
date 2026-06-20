package com.example.gocode.lessons

object CurriculumRepository {
    fun section1(language: String): List<PathNodeItem> {
        return when (language.lowercase()) {
            "python" -> pythonSection1()
            "java" -> javaSection1()
            "c" -> cSection1()
            else -> pythonSection1()
        }
    }

    fun applyProgress(
        template: List<PathNodeItem>,
        completedIds: Set<String>
    ): List<PathNodeItem> {
        return applyProgress(template, completedIds.associateWith { 100 })
    }

    fun applyProgress(
        template: List<PathNodeItem>,
        progressById: Map<String, Int>
    ): List<PathNodeItem> {

        val firstNotCompletedIndex = template.indexOfFirst {
            progressById.getOrDefault(it.id, 0) < 100
        }
            .let { if (it == -1) template.size else it }

        return template.mapIndexed { index, node ->
            val locked = index > firstNotCompletedIndex
            val progress = progressById.getOrDefault(node.id, 0).coerceIn(0, 100)

            node.copy(
                locked = locked,
                progressPercent = progress
            )
        }
    }

    private fun pythonSection1(): List<PathNodeItem> = listOf(
        PathNodeItem(
            id = "py_u1_l1",
            type = PathNodeType.LESSON,
            title = "Print basics",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "py_u1_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: prints",
            offsetDp = 40
        ),
        PathNodeItem(
            id = "py_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "py_u1_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: basics",
            offsetDp = 70
        ),
        PathNodeItem(
            id = "py_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "py_u1_c1",
            type = PathNodeType.CODE,
            title = "Code: Hello World",
            offsetDp = 30
        )
    )

    private fun javaSection1(): List<PathNodeItem> = listOf(
        PathNodeItem(
            id = "java_u1_l1",
            type = PathNodeType.LESSON,
            title = "Program shape + print",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u1_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: first program",
            offsetDp = 40
        ),
        PathNodeItem(
            id = "java_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables + types",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "java_u1_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: variables",
            offsetDp = 70
        ),
        PathNodeItem(
            id = "java_u1_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: basics",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "java_u1_c1",
            type = PathNodeType.CODE,
            title = "Code: Hello World",
            offsetDp = 30
        ),
        PathNodeItem(
            id = "java_u2_l1",
            type = PathNodeType.LESSON,
            title = "Comparisons + if",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u2_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: if",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u2_l2",
            type = PathNodeType.LESSON,
            title = "else + logic",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u2_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: decisions",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u2_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: decisions",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u2_c1",
            type = PathNodeType.CODE,
            title = "Code: Password check",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u3_l1",
            type = PathNodeType.LESSON,
            title = "while loops",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u3_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: while",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u3_l2",
            type = PathNodeType.LESSON,
            title = "for loops",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u3_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: loops",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u3_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: loops",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u3_c1",
            type = PathNodeType.CODE,
            title = "Code: Count loop",
            offsetDp = 36
        )
    )

    private fun cSection1(): List<PathNodeItem> = listOf(
        PathNodeItem(
            id = "c_u1_l1",
            type = PathNodeType.LESSON,
            title = "#include + printf",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "c_u1_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: printf",
            offsetDp = 40
        ),
        PathNodeItem(
            id = "c_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "c_u1_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: basics",
            offsetDp = 70
        ),
        PathNodeItem(
            id = "c_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "c_u1_c1",
            type = PathNodeType.CODE,
            title = "Code: Hello World",
            offsetDp = 30
        )
    )
}
