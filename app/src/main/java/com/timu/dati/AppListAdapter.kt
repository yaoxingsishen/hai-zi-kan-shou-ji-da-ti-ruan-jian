package com.timu.dati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onToggle: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val check: TextView = view.findViewById(R.id.appCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name
        holder.check.text = if (app.monitored) "✓ 已管控" else "○ 添加"
        holder.check.setTextColor(
            if (app.monitored) holder.itemView.context.getColor(R.color.primary)
            else holder.itemView.context.getColor(R.color.text_light)
        )
        holder.itemView.setOnClickListener { onToggle(app) }
    }

    override fun getItemCount() = apps.size
}
