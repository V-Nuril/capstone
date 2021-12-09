package com.project.x1.capstone

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.items.view.*

class Adapter constructor(private val itemClick: OnItemClickListener) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    var list = mutableListOf<Reminder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding(list[position], position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun binding(reminder: Reminder, position: Int) {

            itemView.serialTV.text = "${position + 1}."
            itemView.descriptionTV.text = reminder.description

            val reminderDate =
                Object.getFormattedDate(reminder.date + " " + reminder.time, "dd/MM/YYYY HH:mm")
            if (reminderDate.time < System.currentTimeMillis()) {
                itemView.reminderTV.text = reminder.title
            } else {
                itemView.reminderTV.text = reminder.title
                itemView.serialTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.reminderTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.descriptionTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.timeTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.dateTV.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }

            itemView.timeTV.text = reminder.time
            itemView.dateTV.text = reminder.date

            itemView.more.setOnClickListener {
                itemClick.onItemClick(reminder, itemView.more, adapterPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(
            reminder: Reminder,
            view: View,
            position: Int
        )
    }

}