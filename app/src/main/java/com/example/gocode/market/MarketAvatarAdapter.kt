package com.example.gocode.market

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.models.AvatarItem
import com.example.gocode.repositories.AvatarRepository

class MarketAvatarAdapter(
    private val items: List<AvatarItem>,
    private var profile: MarketProfile,
    private val onBuy: (AvatarItem) -> Unit,
    private val onEquip: (AvatarItem) -> Unit
) : RecyclerView.Adapter<MarketAvatarAdapter.VH>() {

    fun updateProfile(nextProfile: MarketProfile) {
        profile = nextProfile
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_market_avatar, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val isOwned = item.id in profile.ownedAvatarIds
        val isEquipped = item.id == profile.equippedAvatarId

        holder.title.text = item.displayName
        holder.rarity.text = item.rarity
        holder.price.text = if (item.price == 0L) "Free" else "${item.price} coins"
        holder.image.alpha = if (isOwned) 1f else 0.45f
        holder.lock.visibility = if (isOwned) View.GONE else View.VISIBLE

        val resId = AvatarRepository.resolveDrawableResId(context, item.drawableName)
        if (resId != 0) holder.image.setImageResource(resId)

        holder.rarity.setBackgroundResource(rarityBackground(item.rarity))
        holder.action.text = when {
            isEquipped -> "Equipped"
            isOwned -> "Equip"
            else -> "Buy"
        }
        holder.action.isEnabled = !isEquipped
        holder.action.alpha = if (isEquipped) 0.65f else 1f
        holder.action.setBackgroundResource(
            if (isOwned) R.drawable.bg_market_button_secondary else R.drawable.bg_market_button
        )
        holder.action.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            val currentItem = items[position]
            if (currentItem.id in profile.ownedAvatarIds) {
                onEquip(currentItem)
            } else {
                onBuy(currentItem)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun rarityBackground(rarity: String): Int {
        return when (rarity.lowercase()) {
            "starter" -> R.drawable.bg_rarity_starter
            "rare" -> R.drawable.bg_rarity_rare
            "epic" -> R.drawable.bg_rarity_epic
            "legendary" -> R.drawable.bg_rarity_legendary
            "mythic" -> R.drawable.bg_rarity_mythic
            else -> R.drawable.bg_rarity_common
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.marketAvatarImage)
        val lock: View = itemView.findViewById(R.id.marketAvatarLock)
        val title: TextView = itemView.findViewById(R.id.marketAvatarTitle)
        val rarity: TextView = itemView.findViewById(R.id.marketAvatarRarity)
        val price: TextView = itemView.findViewById(R.id.marketAvatarPrice)
        val action: TextView = itemView.findViewById(R.id.marketAvatarAction)
    }
}
