package com.example.petsapp26
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petsapp26.Message
import com.example.petsapp26.R

class ChatAdapter(private val messages: MutableList<Message> = mutableListOf()) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: Message) {
            senderTextView.text = message.sender
            messageTextView.text = message.message
            // Format timestamp if needed
            timestampTextView.text = message.timestamp.toString()
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

    // Helper method to add a new message to the list
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
