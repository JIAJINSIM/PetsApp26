package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class EditPetFragment : Fragment() {

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_pet, container, false)

        etPetName = view.findViewById(R.id.etPetName)
        etPetBreed = view.findViewById(R.id.etPetBreed)
        etPetAge = view.findViewById(R.id.etPetAge)
        etPetGender = view.findViewById(R.id.etPetGender)
        etPetMedicalHistory = view.findViewById(R.id.etPetMedicalHistory)
        btnSavePetDetails = view.findViewById(R.id.btnSavePetDetails)

        // Prepopulate the EditTexts with the pet details
        arguments?.let {
            etPetName.setText(it.getString("name"))
            etPetBreed.setText(it.getString("breed"))
            etPetAge.setText(it.getInt("age").toString())
            etPetGender.setText(it.getString("gender"))
            etPetMedicalHistory.setText(it.getString("medicalHistory"))
        }

        btnSavePetDetails.setOnClickListener {
            savePetDetails()
        }

        return view
    }

    private fun savePetDetails() {
        val petId = arguments?.getString("petId") ?: return // Ensure we have a pet ID
        val petName = etPetName.text.toString().trim()
        val petBreed = etPetBreed.text.toString().trim()
        val petAge = etPetAge.text.toString().toIntOrNull()
        val petGender = etPetGender.text.toString().trim()
        val petMedicalHistory = etPetMedicalHistory.text.toString().trim()

        if (petName.isEmpty() || petBreed.isEmpty() || petAge == null || petGender.isEmpty()) {
            Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val petDetails = hashMapOf<String, Any>(
            "name" to petName,
            "breed" to petBreed,
            "age" to petAge,
            "gender" to petGender,
            "medicalHistory" to petMedicalHistory
        )

        db.collection("Pets").document(petId)
            .update(petDetails)
            .addOnSuccessListener {
                Toast.makeText(activity, "Pet details updated successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to the pet profile fragment
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error updating pet details: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
