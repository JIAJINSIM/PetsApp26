// Profile.kt
package com.example.petsapp26

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Profile : Fragment() {
    private lateinit var profileImageView: ImageView

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profileImage)

        view.findViewById<Button>(R.id.changeProfileImageButton).setOnClickListener {
            openImageChooser()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val firestoreDocumentId = sharedPreferences?.getString("userID", null)

        val username = sharedPreferences?.getString("username", "No Username")
        val usernameTextView: TextView = view.findViewById(R.id.tvUsername)

        val documentId = sharedPreferences?.getString("documentID", "No ID") // Use 'No ID' as default value
        val documentIdTextView: TextView = view.findViewById(R.id.tvUID)

        // Set the TextViews to the retrieved values
        usernameTextView.text = "Username: $username"
        documentIdTextView.text = "Document ID: $documentId"

        // Use the document ID to fetch user profile details
        documentId?.let {
            if (firestoreDocumentId != null && firestoreDocumentId != "No ID") {
                println(firestoreDocumentId)
                loadUserProfile(firestoreDocumentId)
                Toast.makeText(context, "Document ID found", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No Document ID found. Please log in again.", Toast.LENGTH_SHORT).show()
                // Consider navigating back to the login screen
            }
        } ?: run {
            // The document ID is null
            Toast.makeText(context, "No Document ID found. Please log in again.", Toast.LENGTH_SHORT).show()
            // Consider navigating back to the login screen
        }

    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            profileImageView.setImageURI(imageUri)
        }
    }


    private fun loadUserProfile(documentId: String) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(documentId)

        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Extract user details
                    val username = documentSnapshot.getString("username") ?: "Unknown"
                    val role = documentSnapshot.getString("role") ?: "Unknown"
                    val imageUrl = documentSnapshot.getString("profileImageUrl")

                    // Update the TextViews with the user details
                    val usernameTextView: TextView = view?.findViewById(R.id.tvUsername) ?: return@addOnSuccessListener
                    val documentIdTextView: TextView = view?.findViewById(R.id.tvUID) ?: return@addOnSuccessListener

                    usernameTextView.text = "Username: $username"
                    documentIdTextView.text = "Document ID: $documentId"

                    // Load the profile image if available
                    imageUrl?.let { url ->
                        Glide.with(this).load(url).into(profileImageView)
                    }
                } else {
                    Toast.makeText(context, "Profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



}


