package com.example.petsapp26
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(private val messages: MutableList<Message> = mutableListOf()) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: Message) {
            //senderTextView.text = message.sender
            // Check if the message is a location link
            if (message.message.startsWith("Location: https://maps.google.com/?q=")) {
                // This makes sure the link is clickable
                val spannableString = SpannableString(message.message)
                Linkify.addLinks(spannableString, Linkify.WEB_URLS)
                messageTextView.text = spannableString
                messageTextView.movementMethod = LinkMovementMethod.getInstance()
            } else {
                // Normal text message
                messageTextView.text = message.message
            }
            // Format timestamp
            val formattedDateTime = dateTimeFormat.format(message.timestamp.toDate())
            timestampTextView.text = formattedDateTime
            // Use senderUsername instead of sender ID
            senderTextView.text = message.senderUsername
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateData(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()

    }

    fun clearData() {
        messages.clear() // Assuming messageList is the list holding your data.
        notifyDataSetChanged()
    }


}
