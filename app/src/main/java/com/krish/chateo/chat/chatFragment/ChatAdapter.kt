package com.krish.chateo.chat.chatFragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

import com.krish.chateo.R
import com.krish.chateo.model.Inbox
import com.krish.chateo.util.formatAsListItem
import com.krish.chateo.util.formatAsTime
import de.hdodenhof.circleimageview.CircleImageView

interface CardClick{
    fun inBoxClick(uid : String)
}

class ChatAdapter(val context : Context, val listener : ChatFragment) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var inboxList = emptyList<Inbox>()

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val inboxCard : MaterialCardView = itemView.findViewById(R.id.inbox_card)
        val userImage: CircleImageView = itemView.findViewById(R.id.userImgView)
        val username : TextView = itemView.findViewById(R.id.tvUsername)
        val time : TextView = itemView.findViewById(R.id.tvTime)
        val subText : TextView = itemView.findViewById(R.id.tvSubText)
        val count : TextView = itemView.findViewById(R.id.tvCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.apply {
            val inbox = inboxList[position]
            userImage.load(inbox.image){
                error(R.drawable.ic_placeholder)
            }
            username.text = inbox.name
            time.text = inbox.time.formatAsListItem(context)
            subText.text = inbox.message
            count.isVisible = inbox.count > 0
            count.text = inbox.count.toString()
            inboxCard.setOnClickListener {
                listener.inBoxClick(uid = inbox.to)
            }
        }
    }

    override fun getItemCount(): Int {
        return inboxList.size
    }

    fun setData(newInboxList : List<Inbox>){
        inboxList = newInboxList
        notifyDataSetChanged()
    }

}