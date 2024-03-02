package com.example.petsapp26

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class AddPetFragment : Fragment() {

    private lateinit var etPetName: EditText
    private lateinit var etPetBreed: EditText
    private lateinit var etPetAge: EditText
    private lateinit var etPetGender: EditText
    private lateinit var etPetMedicalHistory: EditText
    private lateinit var btnSavePetDetails: Button
    private val db = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_pet, container, false)

        etPetName = view.findViewById(R.id.etPetName)
        etPetBreed = view.findViewById(R.id.etPetBreed)
        etPetAge = view.findViewById(R.id.etPetAge)
        etPetGender = view.findViewById(R.id.etPetGender)
        etPetMedicalHistory = view.findViewById(R.id.etPetMedicalHistory)
        btnSavePetDetails = view.findViewById(R.id.btnSavePetDetails)

        btnSavePetDetails.setOnClickListener {
            val currentUserDocumentId = getCurrentUserDocumentId() // Get the current user's document ID.
            val currentUsername = getCurrentUsername() // Get the current username.
            if (currentUserDocumentId != null && currentUsername != null) {
                savePetDetails(currentUserDocumentId, currentUsername)
                navigateToPetProfileFragment()
            } else {
                Toast.makeText(activity, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun getCurrentUserDocumentId(): String? {
        // Retrieve the current user's document ID from SharedPreferences.
        val prefs = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs?.getString("documentId", null)
    }

    private fun getCurrentUsername(): String? {
        // Retrieve the current username from SharedPreferences.
        val prefs = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs?.getString("username", null)
    }

    private fun savePetDetails(userDocumentId: String, username: String) {
        val petName = etPetName.text.toString().trim()
        val petBreed = etPetBreed.text.toString().trim()
        val petAge = etPetAge.text.toString().toIntOrNull()
        val petGender = etPetGender.text.toString().trim()
        val petMedicalHistory = etPetMedicalHistory.text.toString().trim()

        if (petName.isEmpty() || petBreed.isEmpty() || petAge == null || petGender.isEmpty()) {
            Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val petDetails = hashMapOf(
            "ownerDocumentId" to userDocumentId, // Include the user's document ID.
            "ownerUsername" to username, // Include the username.
            "name" to petName,
            "breed" to petBreed,
            "age" to petAge,
            "gender" to petGender,
            "medicalHistory" to petMedicalHistory
        )

        val petId = "pet_${System.currentTimeMillis()}"

        db.collection("Pets")
            .document(petId)
            .set(petDetails)
            .addOnSuccessListener {
                Toast.makeText(activity, "Pet details saved successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity,
                    "Error saving pet details: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }

    private fun navigateToPetProfileFragment() {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val PetProfileFragment = PetProfileFragment()

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        fragmentTransaction.replace(R.id.fragment_container, PetProfileFragment)
        fragmentTransaction.addToBackStack(null)

        // Commit the transaction
        fragmentTransaction.commit()
    }

}