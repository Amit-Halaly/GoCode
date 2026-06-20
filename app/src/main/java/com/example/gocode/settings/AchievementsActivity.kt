package com.example.gocode.settings

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gocode.R
import com.example.gocode.gamification.AchievementCatalog
import com.example.gocode.gamification.AchievementDefinition
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AchievementsActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var userListener: ListenerRegistration? = null
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        container = findViewById(R.id.achievementsContainer)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        attachUserListener()
    }

    private fun attachUserListener() {
        val user = auth.currentUser
        if (user == null) {
            render(emptySet())
            return
        }

        userListener = db.collection("users").document(user.uid)
            .addSnapshotListener { doc, _ ->
                val unlockedIds = (doc?.get("achievementIds") as? List<*>)
                    .orEmpty()
                    .mapNotNull { it as? String }
                    .toSet()
                render(unlockedIds)
            }
    }

    private fun render(unlockedIds: Set<String>) {
        container.removeAllViews()
        AchievementCatalog.all.forEach { achievement ->
            container.addView(achievementRow(achievement, achievement.id in unlockedIds))
        }
    }

    private fun achievementRow(
        achievement: AchievementDefinition,
        unlocked: Boolean
    ): MaterialCardView {
        return MaterialCardView(this).apply {
            radius = dp(18).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(getColor(R.color.gc_card_background))
            alpha = if (unlocked) 1f else 0.48f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            addView(LinearLayout(this@AchievementsActivity).apply {
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(16), dp(14), dp(16), dp(14))

                addView(ImageView(this@AchievementsActivity).apply {
                    setImageResource(achievement.iconRes)
                    layoutParams = LinearLayout.LayoutParams(dp(54), dp(54)).apply {
                        marginEnd = dp(14)
                    }
                })

                addView(LinearLayout(this@AchievementsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                    addView(TextView(this@AchievementsActivity).apply {
                        text = achievement.title
                        setTextColor(getColor(R.color.gc_text_primary))
                        textSize = 16f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    })

                    addView(TextView(this@AchievementsActivity).apply {
                        text = achievement.description
                        setTextColor(getColor(R.color.gc_text_secondary))
                        textSize = 13f
                        setPadding(0, dp(3), 0, 0)
                    })

                    addView(TextView(this@AchievementsActivity).apply {
                        text = "Reward: ${achievement.rewardText}"
                        setTextColor(getColor(if (unlocked) R.color.practice_correct else R.color.gc_text_secondary))
                        textSize = 12f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        setPadding(0, dp(6), 0, 0)
                    })
                })

                addView(TextView(this@AchievementsActivity).apply {
                    text = if (unlocked) "Unlocked" else "Locked"
                    setTextColor(getColor(if (unlocked) R.color.practice_correct else R.color.gc_text_secondary))
                    textSize = 12f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                })
            })
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
        userListener = null
    }
}
