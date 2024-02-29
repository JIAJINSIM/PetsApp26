package com.example.petsapp26

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.QuerySnapshot


data class Message(
    @PropertyName("sender") val sender: String = "",
    @PropertyName("message") val message: String = "",
    @PropertyName("timestamp") val timestamp: Timestamp = Timestamp.now(),
    @PropertyName("receiver") val receiver: String? = null, // Add this if you are using a receiver field in Firestore
    var senderUsername: String = "" // Temporary field to hold the username once fetched
)

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

class Chat : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter: ChatAdapter // Declare adapter at class level
    private lateinit var recyclerView: RecyclerView


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
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Update UI elements in the layout
        /*userId?.let {
            view.findViewById<TextView>(R.id.textViewUserId)?.text = it
        }*/

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView) // Initialization moved here
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatAdapter()
        recyclerView.adapter = adapter

        fetchMessages()
        view.findViewById<Button>(R.id.buttonSendMessage).setOnClickListener {
        sendMessage()
        view.findViewById<EditText>(R.id.editTextMessage).text.clear() // Clear the input field after sending
    }

    }
    private fun sendMessage() {
        val editTextMessage = view?.findViewById<EditText>(R.id.editTextMessage)
        val messageText = editTextMessage?.text.toString()
        if (messageText.isNotEmpty()) {
            val receiverId = arguments?.getString(ARG_PARAM1)
            val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())

            val message = hashMapOf(
                "sender" to currentUserId,
                "receiver" to receiverId,
                "message" to messageText,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("chats").add(message)
                .addOnSuccessListener {
                    Log.d(TAG, "Message sent successfully")
                    editTextMessage?.text?.clear() // Correctly clear the text field
                    fetchMessages() // Adjusted: No need to pass adapter as it's a class variable
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send message", e)
                }
        }
    }


    private fun fetchMessages() {
        val selectedUserId = arguments?.getString(ARG_PARAM1)
        val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())

        Log.d(TAG, "currentuserID from fetchMessage:, $currentUserId")
        // Start with messages where the current user is the sender.
        firestore.collection("chats")
            .whereEqualTo("sender", currentUserId)
            .whereEqualTo("receiver", selectedUserId)
            .get()
            .addOnSuccessListener { documents1 ->
                val messages = documents1.mapNotNull { it.toObject(Message::class.java) }.toMutableList()

                // Next, fetch messages where the current user is the receiver.
                firestore.collection("chats")
                    .whereEqualTo("sender", selectedUserId)
                    .whereEqualTo("receiver", currentUserId)
                    .get()
                    .addOnSuccessListener { documents2 ->
                        messages.addAll(documents2.mapNotNull { it.toObject(Message::class.java) })

                        // Continue with username fetching and updating UI...
                        fetchUsernames(messages.map { it.sender }.toSet(), messages)
                    }
            }
    }
    private fun fetchUsernames(userIds: Set<String>, messages: MutableList<Message>) {
        val usernameMap = HashMap<String, String>()

        // Create a task list for all fetch operations
        val tasks = userIds.map { userId ->
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Unknown"
                usernameMap[userId] = username
            }
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            // Map usernames to messages
            messages.forEach { message ->
                message.senderUsername = usernameMap[message.sender] ?: "Unknown"
            }

            // Sort messages by timestamp
            messages.sortBy { it.timestamp }

            // Update adapter's data source and UI
            adapter.updateData(messages)
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }
    companion object {
        @JvmStatic
        // Adjust parameter names as necessary
        fun newInstance(selectedUserId: String): Chat {
            val fragment = Chat()
            val args = Bundle().apply {
                putString(ARG_PARAM1, selectedUserId)
                Log.d(TAG, "Select user ID receiver from companion object:, $selectedUserId")
            }
            fragment.arguments = args
            return fragment
        }
    }

}
