package com.example.petsapp26

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.animation.core.exponentialDecay
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class PetProfileFragment : Fragment() {

    private lateinit var listViewPets: ListView
    private lateinit var btnAddPet: Button
    private val db = FirebaseFirestore.getInstance()
    private val petsList = mutableListOf<Map<String, Any>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_petprofile, container, false)

        listViewPets = view.findViewById(R.id.listViewPets)
        btnAddPet = view.findViewById(R.id.btnAddPet)

        btnAddPet.setOnClickListener {
            navigateToAddPetFragment()
        }

        loadPets()

        return view
    }

    private fun loadPets() {
        val currentUserDocumentId = getCurrentUserDocumentId()
        if (currentUserDocumentId != null) {
            db.collection("Pets")
                .whereEqualTo("ownerDocumentId", currentUserDocumentId)
                .get()
                .addOnSuccessListener { documents ->
                    val pets = documents.map { doc ->
                        Pet(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            breed = doc.getString("breed") ?: "",
                            age = doc.getLong("age")?.toInt() ?: 0,
                            gender = doc.getString("gender") ?: "",
                            medicalHistory = doc.getString("medicalHistory") ?: ""
                        )
                    }
                    setupListView(pets)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Error loading pets: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupListView(pets: List<Pet>) {
        val adapter = PetsAdapter(requireContext(), pets, object : PetsAdapter.OnItemClickListener {
            override fun onItemClick(pet: Pet) {
                // Navigate to pet detail fragment or activity
                Toast.makeText(context, "Pet clicked: ${pet.name}", Toast.LENGTH_SHORT).show()
                navigateToAddPetFragment()
            }

            override fun onEditClick(pet: Pet) {
                navigateToEditPetFragment(pet)
            }


            override fun onDeleteClick(pet: Pet, position: Int) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete Pet")
                    setMessage("Are you sure you want to delete this pet?")
                    setPositiveButton("Delete") { dialog, which ->
                        // Step 2: Delete the pet from Firestore
                        confirmAndDeletePet(pet.id, position)
                        loadPets()
                    }
                    setNegativeButton("Cancel", null)
                    show()
                }
            }
        })
        listViewPets.adapter = adapter
    }

    private fun getCurrentUserDocumentId(): String? {
        val prefs = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs?.getString("documentId", null)
    }

    private fun navigateToAddPetFragment() {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val addPetFragment = AddPetFragment()

        fragmentTransaction.replace(R.id.fragment_container, addPetFragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()
    }

    private fun confirmAndDeletePet(documentId: String, position: Int) {
        FirebaseFirestore.getInstance().collection("Pets").document(documentId)
            .delete()
            .addOnSuccessListener {
                // Check if the position is valid before removing from the list
                if (position >= 0 && position < petsList.size) {
                    petsList.removeAt(position)
                    // Notify the adapter directly about the removed item
                    (listViewPets.adapter as? ArrayAdapter<*>)?.notifyDataSetChanged()
                    Toast.makeText(context, "Pet deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // If the position is invalid, simply refresh the list from Firestore
                    loadPets()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting pet: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToEditPetFragment(pet: Pet) {
        val editPetFragment = EditPetFragment().apply {
            arguments = Bundle().apply {
                putString("petId", pet.id) // Pass the Firestore document ID
                putString("name", pet.name)
                putString("breed", pet.breed)
                putInt("age", pet.age)
                putString("gender", pet.gender)
                putString("medicalHistory", pet.medicalHistory)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, editPetFragment)
            .addToBackStack(null) // Add transaction to the back stack for navigation
            .commit()
    }

}


