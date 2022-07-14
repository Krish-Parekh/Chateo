package com.krish.chateo.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.krish.chateo.R
import com.krish.chateo.model.ChatEvent
import com.krish.chateo.model.DateHeader
import com.krish.chateo.model.Message
import com.krish.chateo.util.formatAsTime

class MessageAdapter(private val currentUid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatList = emptyList<ChatEvent>()

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeader = itemView.findViewById<TextView>(R.id.dateHeader)
        fun bind(position: Int) {
            val item = chatList[position]
            if (item is DateHeader) {
                dateHeader.text = item.date
            }
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message : TextView = itemView.findViewById(R.id.message)
        val messageTime : TextView = itemView.findViewById(R.id.messageTime)

        fun bind(position: Int){
            val item = chatList[position]
            if (item is Message){
                message.text = item.message
                messageTime.text = item.sentAt.formatAsTime()
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (val event = chatList[position]) {
            is Message -> {
                if (event.senderId == currentUid) {
                    TEXT_MESSAGE_SENT
                } else {
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DateHeader -> {
                DATE_HEADER
            }
            else -> {
                UNSUPPORTED
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when (viewType) {
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv))
            }
            TEXT_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent))
            }
            DATE_HEADER -> {
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else -> MessageViewHolder(inflate(R.layout.list_item_chat_recv))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = chatList[position]) {
            is DateHeader -> {
                (holder as DateViewHolder).bind(position)
            }
            is Message -> {
                (holder as MessageViewHolder).bind(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }


    fun setData(newChatList : List<ChatEvent>){
        chatList = newChatList
        notifyDataSetChanged()
    }

    companion object {
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }

}