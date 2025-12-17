package com.example.gocode.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.models.AvatarItem
import com.example.gocode.repositories.AvatarRepository

class AvatarAdapter(
    private val items: List<AvatarItem>,
    initiallySelectedId: String,
    private val onSelected: (AvatarItem) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.VH>() {

    private var selectedId: String = initiallySelectedId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        val resId = AvatarRepository.resolveDrawableResId(holder.itemView.context, item.drawableName)
        if (resId != 0) holder.img.setImageResource(resId)

        holder.card.alpha = if (item.id == selectedId) 1f else 0.6f

        holder.card.setOnClickListener {
            val old = selectedId
            selectedId = item.id
            val oldIndex = items.indexOfFirst { it.id == old }
            if (oldIndex != -1) notifyItemChanged(oldIndex)
            notifyItemChanged(position)
            onSelected(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.avatarCard)
        val img: ImageView = itemView.findViewById(R.id.imgAvatar)
    }
}
