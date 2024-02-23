package com.example.petsapp26

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
val userId = FirebaseAuth.getInstance().currentUser?.uid



data class Message(
    val sender: String,
    val message: String,
    val timestamp: Timestamp = Timestamp.now()
)

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Chat : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Get current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch messages from Firestore for the current user
        currentUserId?.let { userId ->
            fetchMessagesFromFirestore(userId) { messages ->
                // Set up adapter with messages retrieved from Firestore
                recyclerView.adapter = ChatAdapter(messages)
            }
        }
    }

    private fun fetchMessagesFromFirestore(userId: String, callback: (MutableList<Message>) -> Unit) {
        firestore.collection("your_messages_collection")
            .whereEqualTo("receiverId", userId) // Assuming each message has a receiver ID
            .get()
            .addOnSuccessListener { documents ->
                val messages = mutableListOf<Message>()
                for (document in documents) {
                    val documentId = document.id // Get the document ID
                    val sender = document.getString("sender") ?: ""
                    val message = document.getString("message") ?: ""
                    val timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
                    messages.add(Message(sender, message, timestamp))
                    Log.d(TAG, "Message retrieved. Document ID: $documentId")
                }
                if (messages.isNotEmpty()) {
                    Log.d(TAG, "Messages retrieved: ${messages.size}")
                } else {
                    Log.d(TAG, "No messages found for user")
                }
                callback(messages)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting messages for user from Firestore", exception)
                // Handle failure gracefully, e.g., show error message to user
            }
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Chat().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
