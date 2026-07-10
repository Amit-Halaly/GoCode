package com.example.gocode.lessons

object CurriculumRepository {
    fun section1(language: String): List<PathNodeItem> {
        return when (language.lowercase()) {
            "python" -> pythonSection1()
            "java" -> javaSection1()
            "c" -> cSection1()
            "cpp", "c++", "cplusplus" -> cppSection1()
            "csharp", "c#", "cs" -> csharpSection1()
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

    private fun pythonSection1(): List<PathNodeItem> = path(
        prefix = "py",
        sections = listOf(
            SectionNodes("Print basics", "Practice: prints", "Variables", "Practice: variables", "Quiz: basics", "Code: Hello World"),
            SectionNodes("Comparisons + if", "Practice: if", "else + logic", "Practice: decisions", "Quiz: if / else", "Code: Access check"),
            SectionNodes("while loops", "Practice: while", "for loops", "Practice: loops", "Quiz: loops", "Code: Count loop"),
            SectionNodes("Lists", "Practice: lists", "List changes", "Practice: indexes", "Quiz: lists", "Code: Favorite list"),
            SectionNodes("Functions", "Practice: functions", "Parameters + return", "Practice: return", "Quiz: functions", "Code: Helper function"),
            SectionNodes("Input", "Practice: input", "Input decisions", "Practice: input logic", "Quiz: input", "Code: Ask age"),
            SectionNodes("String tools", "Practice: strings", "String checks", "Practice: text", "Quiz: strings", "Code: Name checker"),
            SectionNodes("Dictionaries", "Practice: dictionaries", "Dictionary updates", "Practice: records", "Quiz: dictionaries", "Code: Student dictionary"),
            SectionNodes("Reading errors", "Practice: errors", "try / except", "Practice: debugging", "Quiz: debugging", "Code: Safe number"),
            SectionNodes("Final review", "Practice: review", "Build confidence", "Practice: mixed", "Final quiz", "Code: Final check")
        )
    )

    private fun path(prefix: String, sections: List<SectionNodes>): List<PathNodeItem> {
        return sections.flatMapIndexed { sectionIndex, section ->
            val unit = sectionIndex + 1
            listOf(
                PathNodeItem("${prefix}_u${unit}_l1", PathNodeType.LESSON, section.lesson1, offsetDp = 0),
                PathNodeItem("${prefix}_u${unit}_p1", PathNodeType.PRACTICE, section.practice1, offsetDp = 45),
                PathNodeItem("${prefix}_u${unit}_l2", PathNodeType.LESSON, section.lesson2, offsetDp = 12),
                PathNodeItem("${prefix}_u${unit}_p2", PathNodeType.PRACTICE, section.practice2, offsetDp = 72),
                PathNodeItem("${prefix}_u${unit}_q1", PathNodeType.QUIZ, section.quiz, offsetDp = 18),
                PathNodeItem("${prefix}_u${unit}_c1", PathNodeType.CODE, section.code, offsetDp = 36)
            )
        }
    }

    private data class SectionNodes(
        val lesson1: String,
        val practice1: String,
        val lesson2: String,
        val practice2: String,
        val quiz: String,
        val code: String
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
        ),
        PathNodeItem(
            id = "java_u4_l1",
            type = PathNodeType.LESSON,
            title = "Arrays",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u4_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: arrays",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u4_l2",
            type = PathNodeType.LESSON,
            title = "Array indexes",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u4_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: indexes",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u4_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: arrays",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u4_c1",
            type = PathNodeType.CODE,
            title = "Code: Favorite list",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u5_l1",
            type = PathNodeType.LESSON,
            title = "Methods",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u5_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: methods",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u5_l2",
            type = PathNodeType.LESSON,
            title = "Parameters + return",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u5_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: return",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u5_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: methods",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u5_c1",
            type = PathNodeType.CODE,
            title = "Code: Helper method",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u6_l1",
            type = PathNodeType.LESSON,
            title = "Scanner input",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u6_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: input",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u6_l2",
            type = PathNodeType.LESSON,
            title = "Input decisions",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u6_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: Scanner",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u6_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: input",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u6_c1",
            type = PathNodeType.CODE,
            title = "Code: Ask age",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u7_l1",
            type = PathNodeType.LESSON,
            title = "String tools",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u7_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: strings",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u7_l2",
            type = PathNodeType.LESSON,
            title = "String checks",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u7_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: text",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u7_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: strings",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u7_c1",
            type = PathNodeType.CODE,
            title = "Code: Name checker",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u8_l1",
            type = PathNodeType.LESSON,
            title = "Classes",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u8_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: classes",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u8_l2",
            type = PathNodeType.LESSON,
            title = "Objects",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u8_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: objects",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u8_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: objects",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u8_c1",
            type = PathNodeType.CODE,
            title = "Code: Student object",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u9_l1",
            type = PathNodeType.LESSON,
            title = "Reading errors",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u9_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: errors",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u9_l2",
            type = PathNodeType.LESSON,
            title = "try / catch",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u9_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: debugging",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u9_q1",
            type = PathNodeType.QUIZ,
            title = "Quiz: debugging",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u9_c1",
            type = PathNodeType.CODE,
            title = "Code: Safe parse",
            offsetDp = 36
        ),
        PathNodeItem(
            id = "java_u10_l1",
            type = PathNodeType.LESSON,
            title = "Final review",
            offsetDp = 0
        ),
        PathNodeItem(
            id = "java_u10_p1",
            type = PathNodeType.PRACTICE,
            title = "Practice: review",
            offsetDp = 45
        ),
        PathNodeItem(
            id = "java_u10_l2",
            type = PathNodeType.LESSON,
            title = "Build confidence",
            offsetDp = 12
        ),
        PathNodeItem(
            id = "java_u10_p2",
            type = PathNodeType.PRACTICE,
            title = "Practice: mixed",
            offsetDp = 72
        ),
        PathNodeItem(
            id = "java_u10_q1",
            type = PathNodeType.QUIZ,
            title = "Final quiz",
            offsetDp = 18
        ),
        PathNodeItem(
            id = "java_u10_c1",
            type = PathNodeType.CODE,
            title = "Code: Final check",
            offsetDp = 36
        )
    )

    private fun cSection1(): List<PathNodeItem> = path(
        prefix = "c",
        sections = listOf(
            SectionNodes("#include + printf", "Practice: printf", "Variables + types", "Practice: variables", "Quiz: basics", "Code: Hello C"),
            SectionNodes("Comparisons + if", "Practice: if", "else + logic", "Practice: decisions", "Quiz: decisions", "Code: Access check"),
            SectionNodes("while loops", "Practice: while", "for loops", "Practice: loops", "Quiz: loops", "Code: Count loop"),
            SectionNodes("Arrays", "Practice: arrays", "Array indexes", "Practice: indexes", "Quiz: arrays", "Code: Scores list"),
            SectionNodes("Functions", "Practice: functions", "Parameters + return", "Practice: return", "Quiz: functions", "Code: Helper function"),
            SectionNodes("scanf input", "Practice: input", "Input decisions", "Practice: scanf", "Quiz: input", "Code: Ask age"),
            SectionNodes("Strings as char arrays", "Practice: strings", "String checks", "Practice: text", "Quiz: strings", "Code: Name checker"),
            SectionNodes("Pointers", "Practice: pointers", "Pointers + arrays", "Practice: addresses", "Quiz: pointers", "Code: Swap values"),
            SectionNodes("Reading errors", "Practice: errors", "Debugging C", "Practice: debugging", "Quiz: debugging", "Code: Safe divide"),
            SectionNodes("Final review", "Practice: review", "Build confidence", "Practice: mixed", "Final quiz", "Code: Final check")
        )
    )

    private fun cppSection1(): List<PathNodeItem> = path(
        prefix = "cpp",
        sections = listOf(
            SectionNodes("iostream + cout", "Practice: cout", "Variables + types", "Practice: variables", "Quiz: basics", "Code: Hello C++"),
            SectionNodes("Comparisons + if", "Practice: if", "else + logic", "Practice: decisions", "Quiz: decisions", "Code: Access check"),
            SectionNodes("while loops", "Practice: while", "for loops", "Practice: loops", "Quiz: loops", "Code: Count loop"),
            SectionNodes("Vectors", "Practice: vectors", "Vector changes", "Practice: indexes", "Quiz: vectors", "Code: Scores vector"),
            SectionNodes("Functions", "Practice: functions", "Parameters + return", "Practice: return", "Quiz: functions", "Code: Helper function"),
            SectionNodes("cin input", "Practice: input", "Input decisions", "Practice: cin", "Quiz: input", "Code: Ask age"),
            SectionNodes("String tools", "Practice: strings", "String checks", "Practice: text", "Quiz: strings", "Code: Name checker"),
            SectionNodes("Classes", "Practice: classes", "Methods", "Practice: objects", "Quiz: objects", "Code: Student object"),
            SectionNodes("Reading errors", "Practice: errors", "try / catch", "Practice: debugging", "Quiz: debugging", "Code: Safe parse"),
            SectionNodes("Final review", "Practice: review", "Build confidence", "Practice: mixed", "Final quiz", "Code: Final check")
        )
    )

    private fun csharpSection1(): List<PathNodeItem> = path(
        prefix = "cs",
        sections = listOf(
            SectionNodes("Program shape + print", "Practice: first program", "Variables + types", "Practice: variables", "Quiz: basics", "Code: Hello C#"),
            SectionNodes("Comparisons + if", "Practice: if", "else + logic", "Practice: decisions", "Quiz: decisions", "Code: Access check"),
            SectionNodes("while loops", "Practice: while", "for loops", "Practice: loops", "Quiz: loops", "Code: Count loop"),
            SectionNodes("Arrays", "Practice: arrays", "Array indexes", "Practice: indexes", "Quiz: arrays", "Code: Scores array"),
            SectionNodes("Methods", "Practice: methods", "Parameters + return", "Practice: return", "Quiz: methods", "Code: Helper method"),
            SectionNodes("Console input", "Practice: input", "Input decisions", "Practice: ReadLine", "Quiz: input", "Code: Ask age"),
            SectionNodes("String tools", "Practice: strings", "String checks", "Practice: text", "Quiz: strings", "Code: Name checker"),
            SectionNodes("Classes", "Practice: classes", "Objects", "Practice: objects", "Quiz: objects", "Code: Student object"),
            SectionNodes("Reading errors", "Practice: errors", "try / catch", "Practice: debugging", "Quiz: debugging", "Code: Safe parse"),
            SectionNodes("Final review", "Practice: review", "Build confidence", "Practice: mixed", "Final quiz", "Code: Final check")
        )
    )
}
