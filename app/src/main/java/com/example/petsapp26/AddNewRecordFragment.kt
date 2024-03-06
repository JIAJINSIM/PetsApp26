package com.example.petsapp26

import CustomArrayAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class AddNewRecordFragment : Fragment() {

    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_addnewrecord, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinner = view.findViewById(R.id.custID_dropdown)
        fetchItemsFromFirebase()

        val btnSubmit: Button = view.findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            // Here you would collect the data from the form
            val description = view.findViewById<EditText>(R.id.et_description).text.toString()
            val diagnosis = view.findViewById<EditText>(R.id.et_diagnosis).text.toString()
            // Continue for other fields

            // Then, add the record to Firestore (example)
            val newRecord = hashMapOf(
                "description" to description,
                "diagnosis" to diagnosis
                // Add other fields here
            )

            FirebaseFirestore.getInstance().collection("Records").add(newRecord)
                .addOnSuccessListener {
                    // Handle success, maybe navigate back or show a success message
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    private fun fetchItemsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val items = mutableListOf("Select CustomerID")

        // Query the "users" collection for documents where the role is "user"
        db.collection("users")
            .whereEqualTo("role", "user")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Get the document ID
                    val userId = document.id
                    items.add(userId)
                }
                if (items.isNotEmpty()) {
                    val adapter = CustomArrayAdapter(requireContext(), items)
                    spinner.adapter = adapter
                    spinner.setSelection(0)
                } else {
                    // Handle the case where no users with the role "user" were found
                }
            }.addOnFailureListener { exception ->
                // Handle any errors here
            }
    }
}

