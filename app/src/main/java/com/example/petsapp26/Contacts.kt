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

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "ContactsFragment"
// Inside your Contacts fragment
//val currentUser = FirebaseAuth.getInstance().currentUser
//val userId = currentUser?.uid
class Contacts : Fragment() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }
    // Function to receive the user ID
    fun receiveUserId(userId: String) {
        // Use the user ID here in the Contacts fragment
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
        /// Get the current user's ID
        FirebaseAuth.getInstance().currentUser
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        // Log the current user's ID for debugging
        Log.d(TAG, "Current User ID: $currentUserID")
        // Query Firestore to check if a conversation exists between the current user and the selected contact
        firestore.collection("conversations")
            .whereEqualTo("participants.$currentUserID", true)
            .whereEqualTo("participants.$contactUsername", true)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // If conversation exists, navigate to Chat fragment to show chat history
                    val chatFragment = Chat.newInstance(contactUsername, "admin")
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, chatFragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    // If conversation does not exist, create a new conversation
                    createNewConversation(contactUsername)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking conversation existence: $exception")
                // Handle failure gracefully, e.g., show error message to user
            }
    }
    private fun createNewConversation(contactUsername: String) {
        // Here you would create a new conversation document in your Firestore database
        // For simplicity, let's just print a log message
        Log.d(TAG, "Creating new conversation with $contactUsername")
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

