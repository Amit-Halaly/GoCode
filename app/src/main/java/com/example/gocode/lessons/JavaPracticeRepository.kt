package com.example.gocode.lessons

object JavaPracticeRepository {

    fun getPractice1Questions(): List<PracticeQuestion> {
        return listOf(
            PracticeQuestion(
                id = "java_p1_q1",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 1",
                question = "Which command prints text to the console in Java?",
                options = listOf(
                    "System.out.println()",
                    "print.console()",
                    "Console.write()",
                    "echo()"
                ),
                correctAnswer = "System.out.println()",
                explanation = "In Java, System.out.println() is used to print text to the console."
            ),
            PracticeQuestion(
                id = "java_p1_q2",
                type = PracticeQuestionType.DRAG_FILL_BLANK,
                title = "Question 2",
                question = "Drag the correct command into the blank to print Hello World.",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            ______("Hello World");
                        }
                    }
                """.trimIndent(),
                options = listOf(
                    "System.out.println",
                    "Console.write",
                    "print.console",
                    "echo"
                ),
                correctAnswer = "System.out.println",
                explanation = "The full command is System.out.println(\"Hello World\");"
            ),
            PracticeQuestion(
                id = "java_p1_q3",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 3",
                question = "What does println mean?",
                options = listOf(
                    "Print line",
                    "Print language",
                    "Private line",
                    "Program link"
                ),
                correctAnswer = "Print line",
                explanation = "println means print line. It prints text and moves to the next line."
            ),
            PracticeQuestion(
                id = "java_p1_q4",
                type = PracticeQuestionType.FILL_BLANK,
                title = "Question 4",
                question = "Fill in the missing text inside the quotes.",
                code = """
                    System.out.println("______");
                """.trimIndent(),
                correctAnswer = "Hello World",
                explanation = "The text inside the quotes is the message printed to the console."
            ),
            PracticeQuestion(
                id = "java_p1_q5",
                type = PracticeQuestionType.MULTIPLE_CHOICE,
                title = "Question 5",
                question = "Where does Java start running the program?",
                options = listOf(
                    "main()",
                    "start()",
                    "run()",
                    "print()"
                ),
                correctAnswer = "main()",
                explanation = "Java starts running the program from the main() method."
            )
        )
    }
}
