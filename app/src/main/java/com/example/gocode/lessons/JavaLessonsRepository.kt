package com.example.gocode.lessons.lesson

import com.example.gocode.lessons.LessonStep

object JavaLessonsRepository {

    fun getLesson1Steps(): List<LessonStep> {
        return listOf(
            LessonStep(
                id = "java_l1_s1",
                title = "Welcome to Java 👋",
                body = "Java is a programming language used to build apps, games, and big systems.\n\nLet’s write your first real Java program step-by-step."
            ), LessonStep(
                id = "java_l1_s2",
                title = "A Java program has a class",
                body = "In Java, your code lives inside a class.\n\nMost beginners start with a class called Main:",
                code = """
                    public class Main {
                    }
                """.trimIndent(),
                tip = "Don’t worry about the word 'public' yet. You’ll understand it soon."
            ), LessonStep(
                id = "java_l1_s3",
                title = "main() starts the program",
                body = "Java starts running your code from a special method called main().\n\nThis is your program entry point:",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                        }
                    }
                """.trimIndent(),
                tip = "Think of main() as the START button of your program."
            ), LessonStep(
                id = "java_l1_s4",
                title = "Print to the console",
                body = "To show text on the screen, we use:",
                code = """
                    System.out.println("Hello World");
                """.trimIndent(),
                tip = "println means: print line (prints + goes to next line)."
            ), LessonStep(
                id = "java_l1_s5",
                title = "Your first Java program ✅",
                body = "Here is the full working program:\n\nRead it once and make sure it makes sense.",
                code = """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("Hello World");
                        }
                    }
                """.trimIndent(),
                tip = "In the next step, you’ll practice with questions + fill in the blanks."
            )
        )
    }
}
