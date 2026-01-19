package com.example.gocode

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationsAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<NotificationsAdapter.VH>() {

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
    ) {
        val txt: TextView = itemView.findViewById(R.id.txtNotification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.txt.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}
