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

        val firstNotCompletedIndex = template.indexOfFirst { it.id !in completedIds }
            .let { if (it == -1) template.size else it }

        return template.mapIndexed { index, node ->
            val completed = node.id in completedIds

            val locked = index > firstNotCompletedIndex
            val progress = if (completed) 100 else 0

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
            title = "main() + println",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u1_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: println",
            offsetDp = 40
        ),
        PathNodeItem(
            id = "java_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables + types",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "java_u1_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: syntax",
            offsetDp = 70
        ),
        PathNodeItem(
            id = "java_u1_l2",
            type = PathNodeType.LESSON,
            title = "Variables + types",
            offsetDp = 10
        ),
        PathNodeItem(
            id = "java_u1_c1",
            type = PathNodeType.CODE,
            title = "Code: Hello World",
            offsetDp = 30
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
