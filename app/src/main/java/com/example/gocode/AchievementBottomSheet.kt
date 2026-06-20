package com.example.gocode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.gocode.gamification.GamificationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AchievementBottomSheet : BottomSheetDialogFragment() {

    var onContinue: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_achievement, container, false)

        val icon = view.findViewById<ImageView>(R.id.bsIcon)
        val title = view.findViewById<TextView>(R.id.bsTitle)
        val description = view.findViewById<TextView>(R.id.bsDescription)
        val rewardRow = view.findViewById<LinearLayout>(R.id.rewardRow)
        val xp = view.findViewById<TextView>(R.id.bsXp)
        val coins = view.findViewById<TextView>(R.id.bsCoins)
        val bonus = view.findViewById<TextView>(R.id.bsBonus)
        val achievements = view.findViewById<TextView>(R.id.bsAchievements)
        val continueButton = view.findViewById<MaterialButton>(R.id.bsContinue)

        icon.setImageResource(requireArguments().getInt(ARG_ICON))
        title.text = requireArguments().getString(ARG_TITLE)
        description.text = requireArguments().getString(ARG_DESC)

        val xpValue = requireArguments().getLong(ARG_XP, 0L)
        val coinsValue = requireArguments().getLong(ARG_COINS, 0L)
        val bonusText = requireArguments().getString(ARG_BONUS).orEmpty()
        val achievementText = requireArguments().getString(ARG_ACHIEVEMENTS).orEmpty()

        if (xpValue > 0 || coinsValue > 0) {
            rewardRow.visibility = View.VISIBLE
            xp.text = "+$xpValue XP"
            coins.text = "+$coinsValue Coins"
        }

        if (bonusText.isNotBlank()) {
            bonus.visibility = View.VISIBLE
            bonus.text = bonusText
        }

        if (achievementText.isNotBlank()) {
            achievements.visibility = View.VISIBLE
            achievements.text = achievementText
        }

        continueButton.setOnClickListener { dismiss() }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onContinue?.invoke()
        onContinue = null
    }

    companion object {
        private const val ARG_ICON = "icon"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"
        private const val ARG_XP = "xp"
        private const val ARG_COINS = "coins"
        private const val ARG_BONUS = "bonus"
        private const val ARG_ACHIEVEMENTS = "achievements"

        fun newInstance(icon: Int, title: String, desc: String): AchievementBottomSheet {
            return AchievementBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ICON, icon)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, desc)
                }
            }
        }

        fun newRewardInstance(result: GamificationResult): AchievementBottomSheet {
            val achievementsText = result.newAchievements.joinToString(separator = "\n") {
                "Unlocked: ${it.title}"
            }
            val levelText = if (result.leveledUp) {
                "Level ${result.levelAfter} reached"
            } else {
                ""
            }
            val bonusText = buildList {
                if (levelText.isNotBlank()) add(levelText)
                if (result.bonusCoins > 0) add("+${result.bonusCoins} level bonus coins")
            }.joinToString(separator = "  •  ")

            val title = when {
                result.leveledUp && result.newAchievements.isNotEmpty() -> "Level Up & Achievement"
                result.leveledUp -> "Level Up"
                result.newAchievements.isNotEmpty() -> "Achievement Unlocked"
                else -> "Rewards Claimed"
            }

            val description = when {
                result.leveledUp && result.newAchievements.isNotEmpty() ->
                    "Great run. You climbed a level and unlocked something new."
                result.leveledUp ->
                    "You earned enough XP to reach the next level."
                result.newAchievements.isNotEmpty() ->
                    "A new milestone was added to your profile."
                else ->
                    result.reward.title
            }

            return AchievementBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ICON, R.drawable.trophy)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, description)
                    putLong(ARG_XP, result.reward.xp)
                    putLong(ARG_COINS, result.reward.coins)
                    putString(ARG_BONUS, bonusText)
                    putString(ARG_ACHIEVEMENTS, achievementsText)
                }
            }
        }
    }
}
