package com.example.petsapp26

import CustomArrayAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class AddNewRecordFragment : Fragment() {

    private lateinit var spinner_custID: Spinner
    private lateinit var spinner_apptID: Spinner

    // Variables to store selected IDs
    private var selectedUserID: String? = ""
    private var selectedApptID: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_addnewrecord, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinner_custID = view.findViewById(R.id.custID_dropdown)
        fetchItemsFromFirebase()

        // Initialize the spinner and set it to be not clickable initially
        spinner_apptID = view.findViewById(R.id.apptID_dropdown)
        spinner_apptID.isClickable = false
        spinner_apptID.isEnabled = false // Disable the spinner

        // Set up listener for customer ID spinner
        spinner_custID.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Enable the apptID spinner only if a valid userID is selected (not the hint)
                spinner_apptID.isClickable = position > 0
                spinner_apptID.isEnabled = position > 0
                if (position > 0) {
                    selectedUserID = parent.getItemAtPosition(position) as String
                    Log.d("FetchAppt", "Selected User ID: $selectedUserID")
                    fetchApptIDFromFirebase(selectedUserID!!)
                } else {
                    selectedUserID = ""
                    // If "Select CustomerID" (hint) is selected, ensure apptID spinner is not clickable
                    spinner_apptID.isClickable = false
                    spinner_apptID.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // If nothing is selected, the apptID spinner should not be clickable
                spinner_apptID.isClickable = false
                spinner_apptID.isEnabled = false
            }
        }

        // Listener for appointment ID spinner
        spinner_apptID.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position > 0) {
                    selectedApptID = parent.getItemAtPosition(position) as String
                } else {
                    selectedApptID = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedApptID = null
            }
        }


        val btnSubmit: Button = view.findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            // Here you would collect the data from the form
            val description = view.findViewById<EditText>(R.id.et_description).text.toString()
            val diagnosis = view.findViewById<EditText>(R.id.et_diagnosis).text.toString()
            val symptoms = view.findViewById<EditText>(R.id.et_symptoms).text.toString()
            val treatment = view.findViewById<EditText>(R.id.et_treatment).text.toString()
            val prescription = view.findViewById<EditText>(R.id.et_prescription).text.toString()

            if (selectedUserID == null || selectedApptID == null) {
                Toast.makeText(requireContext(), "Please select a valid customer and an appointment.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Convert selected IDs to DocumentReferences
            val custRef = FirebaseFirestore.getInstance().collection("users").document(selectedUserID!!)
            val apptRef = FirebaseFirestore.getInstance().collection("Appointments").document(selectedApptID!!)


            // Then, add the record to Firestore
            val newRecord = hashMapOf(
                "custID" to custRef,
                "apptID" to apptRef,
                "description" to description,
                "diagnosis" to diagnosis,
                "symptoms" to symptoms,
                "treatment" to treatment,
                "prescription" to prescription
            )

            FirebaseFirestore.getInstance().collection("Records").add(newRecord)
                .addOnSuccessListener {
                    // Handle success, maybe navigate back or show a success message
                    Toast.makeText(requireContext(), "Record added successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // handle failure
                    Toast.makeText(requireContext(), "Error adding record: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                    spinner_custID.adapter = adapter
                    spinner_custID.setSelection(0)
                } else {
                    // Handle the case where no users with the role "user" were found
                }
            }.addOnFailureListener { exception ->
                // Handle any errors here
            }
    }

    private fun fetchApptIDFromFirebase(selectedCustID: String) {
        val db = FirebaseFirestore.getInstance()
        val appointmentsList = mutableListOf("Select AppointmentID")

        // First, fetch all apptID paths that are already in Records to filter them out later
        db.collection("Records")
            .get()
            .addOnSuccessListener { recordsSnapshot ->
                val usedApptIDs = recordsSnapshot.documents.mapNotNull {
                    // Extract just the document ID from the apptID path
                    it.getDocumentReference("apptID")?.path?.split("/")?.last()
                }.toSet()

                // Create a DocumentReference to the selected customer
                val userRef = db.collection("users").document(selectedCustID)

                // Query the "Appointments" collection for documents where the custID field matches the selected Customer ID
                db.collection("Appointments")
                    .whereEqualTo("custID", userRef)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d("FetchAppt", "Number of appointments fetched: ${documents.size()}")
                        for (document in documents) {
                            // Extract just the document ID from the full path of the DocumentReference
                            val apptDocumentID = document.reference.path.split("/").last()
                            if (!usedApptIDs.contains(apptDocumentID)) {
                                appointmentsList.add(apptDocumentID)
                            }
                        }

                        if (appointmentsList.size > 1) { // More than just the initial "Select AppointmentID" means we have valid appointments
                            val adapter = CustomArrayAdapter(requireContext(), appointmentsList)
                            spinner_apptID.adapter = adapter
                            spinner_apptID.setSelection(0)
                            spinner_apptID.isClickable = true
                            spinner_apptID.isEnabled = true
                        } else {
                            // Handle case where there are no available appointments
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FetchAppt", "Error fetching appointments: ${exception.message}")
                        // Handle any errors here
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FetchAppt", "Error fetching records: ${exception.message}")
                // Handle any errors here
            }
    }



//    private fun fetchApptIDFromFirebase(selectedCustID: String) {
//        val db = FirebaseFirestore.getInstance()
//        val appointmentsList = mutableListOf("Select AppointmentID")
//
//        // Create a DocumentReference to the selected customer
//        val userRef = db.collection("users").document(selectedCustID)
//
//
//        spinner_apptID.setSelection(0)
//
//
//        // Query the "Appointments" collection for documents where the custID field matches the selected Customer ID
//        db.collection("Appointments")
//            .whereEqualTo(
//                "custID",
//                userRef
//            ) // Assumes custID field in Firestore contains the full path
//            .get()
//            .addOnSuccessListener { documents ->
//                // Log the count of documents fetched
//                Log.d("FetchAppt", "Number of appointments fetched: ${documents.size()}")
//                for (document in documents) {
//                    // Here we can use either the document ID or the apptID field, if you want to use the apptID field from the document change the below line accordingly
//                    val apptId =
//                        document.id // This is the Firestore document ID, used if apptID field is the same as document ID.
//                    // val apptId = document.getString("apptID") // Use if apptID is a field in the Firestore document
//                    appointmentsList.add(apptId)
//                }
//                if (appointmentsList.size > 1) { // More than just the initial "Select AppointmentID" means we have valid appointments
//                    val adapter = CustomArrayAdapter(requireContext(), appointmentsList)
//                    spinner_apptID.adapter = adapter
//                    spinner_apptID.setSelection(0)
//                } else {
//                    // Handle case where there are no appointments for this user
//                }
//            }.addOnFailureListener { exception ->
//                // Handle any errors here, such as a Toast or a log
//            }
//
//    }
}

