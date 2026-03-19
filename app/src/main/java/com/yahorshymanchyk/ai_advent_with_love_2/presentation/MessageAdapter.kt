package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yahorshymanchyk.ai_advent_with_love_2.R
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

class MessageAdapter(
    private val onLongClick: (index: Int) -> Unit
) : ListAdapter<ChatMessage, MessageAdapter.MessageViewHolder>(DIFF_CALLBACK) {

    fun getItems(): List<ChatMessage> = currentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position), position, onLongClick)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: LinearLayout = itemView.findViewById(R.id.messageContainer)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(message: ChatMessage, index: Int, onLongClick: (Int) -> Unit) {
            tvMessage.text = message.content
            if (message.role == ChatMessage.Role.USER) {
                container.gravity = Gravity.END
                tvMessage.setBackgroundResource(R.drawable.bg_bubble_user)
                tvMessage.setTextColor(Color.WHITE)
            } else {
                container.gravity = Gravity.START
                tvMessage.setBackgroundResource(R.drawable.bg_bubble_assistant)
                tvMessage.setTextColor(Color.BLACK)
            }
            tvMessage.setOnLongClickListener {
                onLongClick(index)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
                oldItem == newItem
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
                oldItem == newItem
        }
    }
}
