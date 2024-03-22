package com.example.petsapp26

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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

    // Fetch the current user's UID
//    private val custID by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    private val custID = "logged-in-user-id"

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
            Log.d("AuthCheck", "Current user: ${FirebaseAuth.getInstance().currentUser?.uid}")
            submitAppointment()
        }

    }
    private fun submitAppointment() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (currentUser != null) {
            val userId = currentUser.uid

            // Fetch the user's username from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { userDocument ->
                    val username = userDocument.getString("username") ?: "Unknown User"

                    val dateString = datePickerEditText.text.toString()
                    val timeString = timePickerEditText.text.toString()
                    val description = additionalNotesEditText.text.toString()

                    // Assuming the SimpleDateFormat of your date and time pickers
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                    try {
                        val date = sdf.parse("$dateString $timeString")
                        val timestamp = Timestamp(date)

                        // Create a new appointment map including the title field
                        val appointment = hashMapOf(
                            "custID" to db.document("users/$userId"),
                            "date" to timestamp,
                            "description" to description,
                            "title" to username // Set title to the username
                        )

                        // Add a new document with a generated ID
                        db.collection("Appointments")
                            .add(appointment)
                            .addOnSuccessListener { documentReference ->
                                Log.d("SubmitAppointment", "DocumentSnapshot added with ID: ${documentReference.id}")
                                // Navigate the user away from this page or clear the form here.
                            }
                            .addOnFailureListener { e ->
                                Log.w("SubmitAppointment", "Error adding document", e)
                            }
                    } catch (e: Exception) {
                        Log.e("SubmitAppointment", "Error parsing date/time", e)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SubmitAppointment", "Error fetching user document: ", exception)
                }
        } else {
            Log.e("SubmitAppointment", "No logged in user found")
        }
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
                val teamMembersNames = documents.mapNotNull { it.getString("name") }
                updateSpinnerWithTeamMembers(teamMembersNames)
            }
            .addOnFailureListener { exception ->
                Log.w("ScheduleApptFragment", "Error getting team members: ", exception)
            }
    }

    private fun updateSpinnerWithTeamMembers(teamMembersNames: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, teamMembersNames)
        val spinner: Spinner = requireView().findViewById(R.id.preferredVetSpinner)
        spinner.adapter = adapter
    }


}

