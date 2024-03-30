package com.example.petsapp26

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.rememberTimePickerState
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleApptFragment : Fragment() {

    private lateinit var datePickerEditText: EditText
    private lateinit var timePickerEditText: EditText
    private lateinit var submitBookingButton: Button
    private lateinit var additionalNotesEditText: EditText
    private lateinit var preferredVetSpinner: Spinner
    //private lateinit var vetName: TextView

    private val db = FirebaseFirestore.getInstance()

    // Fetch the current user's UID
//    private val custID by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    private val custID = "logged-in-user-id"

    data class VetInfo(val name: String, val uid: String) {
        override fun toString(): String = name
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_scheduleappt, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDateAndTimePickers(view)

        arguments?.let { bundle ->
            val vetName = bundle.getString("vetName", "")
            val vetServices = bundle.getString("vetServices", "")
            val vetRating = bundle.getString("vetRating", "")

            view.findViewById<TextView>(R.id.vetName).text = vetName
            view.findViewById<TextView>(R.id.vetServices).text = "Services provided: $vetServices"
            view.findViewById<TextView>(R.id.vetRating).text = "Rating: $vetRating"
        }

        arguments?.getString("vetName")?.let { vetName ->
            fetchTeamMembersForVet(vetName)
        }

        additionalNotesEditText = view.findViewById(R.id.additionalNotes)
        preferredVetSpinner = view.findViewById(R.id.preferredVetSpinner)
        submitBookingButton = view.findViewById(R.id.submitBookingButton)
        submitBookingButton.setOnClickListener {
            val currentUserDocumentId = getCurrentUserDocumentId() // Get the current user's document ID.
            val currentUsername = getCurrentUsername() // Get the current username.
            if (currentUserDocumentId != null && currentUsername != null) {
                submitAppointment(currentUserDocumentId, currentUsername)
                navigateToUserApptFragment()
            } else {
                Toast.makeText(activity, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
            Log.d("AuthCheck", "Current user: ${FirebaseAuth.getInstance().currentUser?.uid}")
            //submitAppointment()
        }


    }
    private fun submitAppointment(userDocumentId: String, username: String,) {
        //val currentUser = FirebaseAuth.getInstance().currentUser
        //val db = FirebaseFirestore.getInstance()
        val preferredVet = preferredVetSpinner.selectedItem.toString()
        val vetNameTextView = view?.findViewById<TextView>(R.id.vetName)
        val dateString = datePickerEditText.text.toString()
        val timeString = timePickerEditText.text.toString()
        val description = additionalNotesEditText.text.toString().trim()
        val selectedVetInfo = preferredVetSpinner.selectedItem as VetInfo
        val vetUID = selectedVetInfo.uid
        val apptId = "appt_${System.currentTimeMillis()}"


        if (dateString.isEmpty() || timeString.isEmpty()) {
            Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val appointment = hashMapOf(
            "Veterinarian Name" to preferredVet,
            "Veterinarian UID" to vetUID,
            "Vet Name" to vetNameTextView?.text.toString(),
            "UserID" to userDocumentId, // Include the user's document ID.
            "Username" to username, // Include the username.
            "Date" to dateString,
            "Time" to timeString,
            "Description" to description,
        )

        db.collection("Appointments")
            .document(apptId)
            .set(appointment)
            .addOnSuccessListener {
                Toast.makeText(activity, "Appointment is made successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity,
                    "Error saving Appointment: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }

//        if (currentUser != null) {
//            val userId = currentUser.uid
//
//
//            // Fetch the user's username from Firestore
//            db.collection("users").document(userId).get()
//                .addOnSuccessListener { userDocument ->
//                    val username = userDocument.getString("username") ?: "Unknown User"
//
//                    val dateString = datePickerEditText.text.toString()
//                    val timeString = timePickerEditText.text.toString()
//                    val description = additionalNotesEditText.text.toString()
//
//                    // Assuming the SimpleDateFormat of your date and time pickers
//                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//
//                    try {
//                        val date = sdf.parse("$dateString $timeString")
//                        val timestamp = Timestamp(date)
//
//                        // Create a new appointment map including the title field
//                        val appointment = hashMapOf(
//                            "ownerDocumentId" to userDocumentId,
//                            "ownerUsername" to username,
//                            "custID" to db.document("users/$userId"),
//                            "date" to timestamp,
//                            "description" to description,
//                            "title" to username // Set title to the username
//                        )
//
//                        // Add a new document with a generated ID
//                        db.collection("Appointments")
//                            .add(appointment)
//                            .addOnSuccessListener { documentReference ->
//                                Log.d("SubmitAppointment", "DocumentSnapshot added with ID: ${documentReference.id}")
//                                // Navigate the user away from this page or clear the form here.
//                                Toast.makeText(activity, "Appointment made successfully", Toast.LENGTH_SHORT)
//                                    .show()
//                            }
//                            .addOnFailureListener { e ->
//                                Log.w("SubmitAppointment", "Error adding document", e)
//                                Toast.makeText(
//                                    activity,
//                                    "Error saving appointment: ${e.localizedMessage}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                    } catch (e: Exception) {
//                        Log.e("SubmitAppointment", "Error parsing date/time", e)
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("SubmitAppointment", "Error fetching user document: ", exception)
//                }
//        } else {
//            Log.e("SubmitAppointment", "No logged in user found")
//        }
    }

    private fun setupDateAndTimePickers(view: View) {
        datePickerEditText = view.findViewById(R.id.datePicker)
        timePickerEditText = view.findViewById(R.id.timePicker)

        val calendar = Calendar.getInstance()

        // Set up DatePickerDialog to start from the next day
        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            // Formatting the date as dd/MM/yyyy
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            datePickerEditText.setText(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // Set the minimum date to the next day
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerEditText.setOnClickListener {
            datePickerDialog.show()
        }

        val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            // Formatting the time as HH:mm
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            timePickerEditText.setText(formattedTime)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        timePickerEditText.setOnClickListener {
            timePickerDialog.show()
        }
    }

    private fun fetchTeamMembersForVet(vetName: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinaries")
            .whereEqualTo("name", vetName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val vetDocId = document.id
                    // Now fetch the team members from the subcollection
                    fetchTeamMembers(vetDocId)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ScheduleApptFragment", "Error getting documents: ", exception)
            }
    }

    private fun fetchTeamMembers(vetDocId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("veterinaries").document(vetDocId).collection("teamMembers")
            .get()
            .addOnSuccessListener { documents ->
                val teamMembersNames = documents.mapNotNull { document ->
                    val name = document.getString("name")
                    val uid = document.getString("uid")
                    if (name != null) uid?.let { VetInfo(name, it) } else null
                }
                updateSpinnerWithTeamMembers(teamMembersNames)
            }
            .addOnFailureListener { exception ->
                Log.w("ScheduleApptFragment", "Error getting team members: ", exception)
            }
    }

    private fun updateSpinnerWithTeamMembers(teamMembersNames: List<VetInfo>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, teamMembersNames)
        val spinner: Spinner = requireView().findViewById(R.id.preferredVetSpinner)
        spinner.adapter = adapter
    }

    private fun getCurrentUsername(): String? {
        // Retrieve the current username from SharedPreferences.
        val prefs = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs?.getString("username", null)
    }

    private fun getCurrentUserDocumentId(): String? {
        // Retrieve the current user's document ID from SharedPreferences.
        val prefs = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs?.getString("documentId", null)
    }

    private fun navigateToUserApptFragment() {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val userApptFragment = UserApptFragment()

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        fragmentTransaction.replace(R.id.fragment_container, userApptFragment)
        fragmentTransaction.addToBackStack(null)

        // Commit the transaction
        fragmentTransaction.commit()
    }


}

