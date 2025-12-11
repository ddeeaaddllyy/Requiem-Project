package com.application.requiemproject.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt
import com.application.requiemproject.R

class HelpAdapter(
    private var items: List<HelpItem>,
    private val onClick: (HelpItem) -> Unit
) : RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    lateinit var textView: TextView

    class HelpViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.text_question_title)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_question, parent, false)

        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.question

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<HelpItem>) {
        items = newList
        notifyDataSetChanged()
    }

}