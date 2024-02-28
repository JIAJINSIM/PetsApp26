package com.example.petsapp26

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth




data class User(
    val username: String = "",
    val role: String = ""
)

data class UserData(val userId: String)

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "ContactsFragment"


// Inside your Contacts fragment
//val currentUser = FirebaseAuth.getInstance().currentUser
//val userId = currentUser?.uid
class Contacts : Fragment() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    fun setUserId(userId: String) {
        this.userId = userId
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }
    // Function to receive the user ID
    fun receiveUserId(userId: String) {
        // Use the user ID here in the Contacts fragment
        this.userId = userId
        Log.d(TAG, "Received User ID: $userId")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchStaffMembers()
    }

    fun fetchStaffMembers() {
        firestore.collection("users")
            .whereEqualTo("role", "admin")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "No documents found for users with admin role.")
                } else {
                    val staffList = documents.toObjects(User::class.java)
                    initRecyclerViewAdapter(staffList)
                    Log.d(TAG, "Found existing DB for users with admin role.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun initRecyclerViewAdapter(staffList: List<User>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewStaff)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = StaffListAdapter(staffList) { user ->
            // Handle click event here, navigate to chat fragment
            checkExistingConversation(user.username)

        }
    }
    private fun checkExistingConversation(contactUsername: String) {
        // Query Firestore to find the user document with the selected contact's username
        firestore.collection("users")
            .whereEqualTo("username", contactUsername)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Retrieve the document ID of the user with the selected contact's username
                    val documentId = documents.documents.firstOrNull()?.id
                    documentId?.let { userId ->
                        // If a document ID is found, navigate to the chat fragment with the user's ID
                        navigateToChatFragment(userId)
                        Log.e(TAG, "Trying to find the receiver userid $userId")
                    } ?: run {
                        // If no document ID is found, log an error or handle it accordingly
                        Log.e(TAG, "No document ID found for username: $contactUsername")
                    }
                } else {
                    // If no user document is found with the selected contact's username, handle it accordingly
                    Log.d(TAG, "No user found with username: $contactUsername")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user document: $exception")
                // Handle failure gracefully, e.g., show error message to user
            }
    }
    /*private fun createNewConversation(contactUsername: String) {
        val currentUserId = PreferencesUtil.getCurrentUserId(requireContext()) ?: return

        val newChatDocument = hashMapOf(
            "sender" to currentUserId,
            "receiver" to contactUsername, // Assuming you store usernames; consider using user IDs instead
            "message" to "Start of conversation", // Placeholder initial message
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("chats").add(newChatDocument)
            .addOnSuccessListener { documentReference ->
                navigateToChatFragment(documentReference.id, contactUsername)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating new conversation", e)
            }
    }*/

    private fun navigateToChatFragment(userId: String) {
        val chatFragment = Chat.newInstance(userId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, chatFragment)
            .addToBackStack(null)
            .commit()
    }
    private fun conversationExists(contactUsername: String): Boolean {
        // Implement logic to check if conversation exists
        // This could involve querying a Firestore collection where conversations are stored
        // For demonstration, we'll return false to simulate no existing conversation
        return false
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Contacts().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

