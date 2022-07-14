package com.krish.chateo.chat.contactFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.krish.chateo.R
import com.krish.chateo.model.User
import de.hdodenhof.circleimageview.CircleImageView

interface CardClick{
    fun onContactClick(contact : User)
}

class ContactAdapter(private val listener : ContactFragment) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contactList = emptyList<User>()

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.profilePicture)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val userCard : MaterialCardView = itemView.findViewById(R.id.userCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.apply {
            val currentContact = contactList[position]
            profileImage.load(currentContact.imageUrl){
                error(R.drawable.ic_placeholder)
            }
            username.text = currentContact.name
            status.text = currentContact.status
            userCard.setOnClickListener {
                listener.onContactClick(contact = currentContact)
            }
        }
    }

    override fun getItemCount(): Int = contactList.size


    fun setData(newContactList : ArrayList<User>){
        contactList = newContactList
        notifyDataSetChanged()
    }

}