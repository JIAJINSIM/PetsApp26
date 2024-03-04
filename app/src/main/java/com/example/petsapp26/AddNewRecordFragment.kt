package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class AddNewRecordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_addnewrecord, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
}
