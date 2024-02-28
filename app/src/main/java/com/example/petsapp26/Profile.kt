// Profile.kt
package com.example.petsapp26

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.IOException

class Profile : Fragment() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var documentIdTextView: TextView
    private var firestoreDocumentId: String? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profileImage)
        usernameTextView = view.findViewById(R.id.tvUsername)
        documentIdTextView = view.findViewById(R.id.tvUID)
        view.findViewById<Button>(R.id.changeProfileImageButton).setOnClickListener {
            openImageChooser()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences =
            activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        firestoreDocumentId = sharedPreferences?.getString("documentId", null)

        val username = sharedPreferences?.getString("username", "No Username")
        val documentId = sharedPreferences?.getString("documentID", "No ID")

        usernameTextView.text = "Username: $username"
        documentIdTextView.text = "Document ID: $documentId"

        firestoreDocumentId?.let {
            if (it != "No ID") {
                loadUserProfile(it)
                loadImageFromInternalStorage(it)
            } else {
                Toast.makeText(context, "Please log in to view profile details", Toast.LENGTH_SHORT)
                    .show()
            }
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

            firestoreDocumentId?.let { userId ->
                saveImageToInternalStorage(imageUri, userId)
            }
        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri, userId: String) {
        val inputStream = activity?.contentResolver?.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val directory = ContextWrapper(activity).getDir("profile", Context.MODE_PRIVATE)
        val mypath = File(directory, "${userId}_profilePic.jpg")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Toast.makeText(activity, "Image Saved to Internal Storage", Toast.LENGTH_SHORT).show()
    }

    private fun loadUserProfile(documentId: String) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(documentId)

        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("username") ?: "Unknown"
                    val imageUrl = documentSnapshot.getString("profileImageUrl")

                    usernameTextView.text = "Username: $username"
                    documentIdTextView.text = "Document ID: $documentId"

                    imageUrl?.let { url ->
                        Glide.with(this).load(url).into(profileImageView)
                    }
                } else {
                    Toast.makeText(context, "Profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun loadImageFromInternalStorage(userId: String) {
        val directory = ContextWrapper(activity).getDir("profile", Context.MODE_PRIVATE)
        val file = File(directory, "${userId}_profilePic.jpg")
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            profileImageView.setImageBitmap(bitmap)
        }
    }
}
