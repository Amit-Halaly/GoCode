package com.example.gocode.firebase

import android.content.Context
import androidx.core.content.edit
import com.example.gocode.gamification.AchievementCatalog
import com.example.gocode.gamification.GamificationRepository
import com.example.gocode.lessons.CurriculumRepository
import com.example.gocode.lessons.LessonStep
import com.example.gocode.lessons.PathNodeItem
import com.example.gocode.lessons.PathNodeType
import com.example.gocode.lessons.PracticeQuestion
import com.example.gocode.lessons.PracticeQuestionType
import com.example.gocode.lessons.JavaPracticeRepository
import com.example.gocode.lessons.JavaLessonsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirebaseContentRepository {
    private const val PREFS_NAME = "firebase_content"
    private const val KEY_SEEDED_VERSION = "seededVersion"
    private const val CONTENT_VERSION = 1

    fun ensureSeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_SEEDED_VERSION, 0) >= CONTENT_VERSION) return

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val nodes = CurriculumRepository.section1("java")

        nodes.forEachIndexed { index, node ->
            val nodeRef = db.collection("curriculum")
                .document("java")
                .collection("nodes")
                .document(node.id)
            batch.set(nodeRef, node.toFirebaseMap(index), SetOptions.merge())

            when (node.type.name) {
                "LESSON" -> {
                    JavaLessonsRepository.getSteps(node.id).forEachIndexed { stepIndex, step ->
                        val stepRef = db.collection("curriculum")
                            .document("java")
                            .collection("lessonSteps")
                            .document(node.id)
                            .collection("steps")
                            .document(step.id)
                        batch.set(stepRef, step.toFirebaseMap(stepIndex), SetOptions.merge())
                    }
                }
                "PRACTICE", "QUIZ" -> {
                    JavaPracticeRepository.getQuestions(node.id).forEachIndexed { questionIndex, question ->
                        val questionRef = db.collection("curriculum")
                            .document("java")
                            .collection("questionSets")
                            .document(node.id)
                            .collection("questions")
                            .document(question.id)
                        batch.set(questionRef, question.toFirebaseMap(questionIndex), SetOptions.merge())
                    }
                }
            }
        }

        codeTasks().forEach { (nodeId, task) ->
            val ref = db.collection("curriculum")
                .document("java")
                .collection("codeTasks")
                .document(nodeId)
            batch.set(ref, task, SetOptions.merge())
        }

        listOf("java_u1_l1", "java_u1_p1", "java_u1_q1", "java_u1_c1").forEach { sampleNodeId ->
            val reward = GamificationRepository.rewardForNode(sampleNodeId)
            val type = when {
                sampleNodeId.contains("_q") -> "QUIZ"
                sampleNodeId.contains("_c") -> "CODE"
                sampleNodeId.contains("_p") -> "PRACTICE"
                else -> "LESSON"
            }
            val rewardRef = db.collection("gamification")
                .document("rewardRules")
                .collection("items")
                .document(type.lowercase())
            batch.set(
                rewardRef,
                mapOf(
                    "type" to type,
                    "xp" to reward.xp,
                    "coins" to reward.coins,
                    "title" to reward.title
                ),
                SetOptions.merge()
            )
        }

        AchievementCatalog.all.forEach { achievement ->
            val ref = db.collection("gamification")
                .document("achievements")
                .collection("items")
                .document(achievement.id)
            batch.set(
                ref,
                mapOf(
                    "id" to achievement.id,
                    "title" to achievement.title,
                    "description" to achievement.description,
                    "rewardText" to achievement.rewardText,
                    "iconName" to achievement.id
                ),
                SetOptions.merge()
            )
        }

        batch.commit().addOnSuccessListener {
            prefs.edit { putInt(KEY_SEEDED_VERSION, CONTENT_VERSION) }
        }
    }

    fun getLessonSteps(
        nodeId: String,
        onResult: (List<LessonStep>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("curriculum")
            .document("java")
            .collection("lessonSteps")
            .document(nodeId)
            .collection("steps")
            .orderBy("order")
            .get()
            .addOnSuccessListener { snapshot ->
                val steps = snapshot.documents.mapNotNull { doc ->
                    LessonStep(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: return@mapNotNull null,
                        body = doc.getString("body") ?: "",
                        code = doc.getString("code"),
                        tip = doc.getString("tip")
                    )
                }
                onResult(steps)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getPathNodes(
        onResult: (List<PathNodeItem>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("curriculum")
            .document("java")
            .collection("nodes")
            .orderBy("order")
            .get()
            .addOnSuccessListener { snapshot ->
                val nodes = snapshot.documents.mapNotNull { doc ->
                    val type = runCatching {
                        PathNodeType.valueOf(doc.getString("type").orEmpty())
                    }.getOrNull() ?: return@mapNotNull null

                    PathNodeItem(
                        id = doc.getString("id") ?: doc.id,
                        type = type,
                        title = doc.getString("title") ?: "",
                        offsetDp = (doc.getLong("offsetDp") ?: 0L).toInt()
                    )
                }
                onResult(nodes)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getQuestions(
        nodeId: String,
        onResult: (List<PracticeQuestion>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("curriculum")
            .document("java")
            .collection("questionSets")
            .document(nodeId)
            .collection("questions")
            .orderBy("order")
            .get()
            .addOnSuccessListener { snapshot ->
                val questions = snapshot.documents.mapNotNull { doc ->
                    val type = runCatching {
                        PracticeQuestionType.valueOf(doc.getString("type").orEmpty())
                    }.getOrNull() ?: return@mapNotNull null

                    PracticeQuestion(
                        id = doc.getString("id") ?: doc.id,
                        type = type,
                        title = doc.getString("title") ?: "",
                        question = doc.getString("question") ?: "",
                        code = doc.getString("code"),
                        options = doc.stringList("options"),
                        correctAnswer = doc.getString("correctAnswer") ?: "",
                        correctAnswers = doc.stringList("correctAnswers"),
                        explanation = doc.getString("explanation") ?: ""
                    )
                }
                onResult(questions)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getCodeTask(
        nodeId: String,
        onResult: (Map<String, String>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("curriculum")
            .document("java")
            .collection("codeTasks")
            .document(nodeId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onResult(emptyMap())
                    return@addOnSuccessListener
                }
                onResult(
                    mapOf(
                        "task" to doc.getString("task").orEmpty(),
                        "template" to doc.getString("template").orEmpty()
                    ).filterValues { it.isNotBlank() }
                )
            }
            .addOnFailureListener { onResult(emptyMap()) }
    }

    private fun PathNodeItem.toFirebaseMap(order: Int): Map<String, Any> = mapOf(
        "id" to id,
        "type" to type.name,
        "title" to title,
        "offsetDp" to offsetDp,
        "order" to order,
        "language" to "java"
    )

    private fun LessonStep.toFirebaseMap(order: Int): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "body" to body,
        "code" to code,
        "tip" to tip,
        "order" to order
    )

    private fun PracticeQuestion.toFirebaseMap(order: Int): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to type.name,
        "title" to title,
        "question" to question,
        "code" to code,
        "options" to options,
        "correctAnswer" to correctAnswer,
        "correctAnswers" to correctAnswers,
        "explanation" to explanation,
        "order" to order
    )

    private fun codeTasks(): Map<String, Map<String, String>> = mapOf(
        "java_u1_c1" to mapOf(
            "task" to "Print Hello GoCode!",
            "template" to """
                public class Main {
                    public static void main(String[] args) {
                        // TODO: Print Hello GoCode!
                    }
                }
            """.trimIndent()
        ),
        "java_u2_c1" to mapOf(
            "task" to "Use if / else to check access. Print Access granted only when age >= 13 and hasPassword is true.",
            "template" to """
                public class Main {
                    public static void main(String[] args) {
                        int age = 16;
                        boolean hasPassword = true;

                        // TODO: Print "Access granted" only when age is at least 13
                        // and hasPassword is true. Otherwise print "Access denied".
                    }
                }
            """.trimIndent()
        ),
        "java_u3_c1" to mapOf("task" to "Use a for loop to print the numbers 1 to 5. When the number is 3, also print Middle.", "template" to "public class Main {\n    public static void main(String[] args) {\n        // TODO: Use a for loop to print the numbers 1 to 5.\n    }\n}"),
        "java_u4_c1" to mapOf("task" to "Use a loop to print every value in the favorites array.", "template" to "public class Main {\n    public static void main(String[] args) {\n        String[] favorites = {\"Java\", \"Android\", \"GoCode\"};\n        // TODO: Print every value.\n    }\n}"),
        "java_u5_c1" to mapOf("task" to "Complete the greet method so it prints Hello plus the name it receives.", "template" to "public class Main {\n    static void greet(String name) {\n        // TODO\n    }\n\n    public static void main(String[] args) {\n        greet(\"Leo\");\n    }\n}"),
        "java_u6_c1" to mapOf("task" to "Use Scanner to read an age. Print Welcome if age is at least 13, otherwise print Too young.", "template" to "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner input = new Scanner(System.in);\n        // TODO\n        input.close();\n    }\n}"),
        "java_u7_c1" to mapOf("task" to "Trim the name and use equals to check if it is Leo. If yes, print Found Leo.", "template" to "public class Main {\n    public static void main(String[] args) {\n        String name = \"  Leo  \";\n        // TODO\n    }\n}"),
        "java_u8_c1" to mapOf("task" to "Complete the Student introduce method so the object prints its own name and age.", "template" to "class Student {\n    String name;\n    int age;\n\n    void introduce() {\n        // TODO\n    }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        Student student = new Student();\n        student.name = \"Maya\";\n        student.age = 14;\n        student.introduce();\n    }\n}"),
        "java_u9_c1" to mapOf("task" to "Use try / catch to parse text into an int. Print the number or Invalid number.", "template" to "public class Main {\n    public static void main(String[] args) {\n        String text = \"42\";\n        // TODO\n    }\n}"),
        "java_u10_c1" to mapOf("task" to "Write a method that loops through the names array and prints only names longer than 3 characters.", "template" to "public class Main {\n    static void printLongNames(String[] names) {\n        // TODO\n    }\n\n    public static void main(String[] args) {\n        String[] names = {\"Leo\", \"Maya\", \"Noam\", \"Dan\"};\n        printLongNames(names);\n    }\n}")
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.stringList(field: String): List<String> {
        return (get(field) as? List<*>).orEmpty().mapNotNull { it as? String }
    }
}
