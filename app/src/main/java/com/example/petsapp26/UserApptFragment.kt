package com.example.petsapp26

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class UserApptFragment : Fragment(), AppointmentActionListener {
    private lateinit var listViewAppts: ListView
    private lateinit var btnBookAppt: Button
    private lateinit var adapter: AppointmentAdapter
    private val db = FirebaseFirestore.getInstance()
    private val appointments = mutableListOf<Appointment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_userappts, container, false)

        listViewAppts = view.findViewById(R.id.listViewAppts)
        btnBookAppt = view.findViewById(R.id.btnBookAppt)

        btnBookAppt.setOnClickListener {
            // Create an instance of the fragment you want to navigate to
            val addApptFragment = AddApptFragment() // Make sure you have this fragment created

            // Perform the fragment transaction to replace the current fragment with the new one
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addApptFragment) // Use the correct container ID
                .addToBackStack(null) // Add this transaction to the back stack
                .commit()
        }

        // Initialize the adapter
        adapter = AppointmentAdapter(
            requireContext(),
            appointments,
            object : AppointmentActionListener {
                override fun onEditAppointment(appointment: Appointment) {
                    // Handle edit action
                }

                override fun deleteAppointment(appointment: Appointment) {
                    this@UserApptFragment.deleteAppointment(appointment)
                }
            },
            AdapterMode.USER
        )
        listViewAppts.adapter = adapter

        loadAppointments()

        return view
    }

    private fun loadAppointments() {
        val currentUserDocumentId = getCurrentUserDocumentId()
        if (currentUserDocumentId != null) {
            db.collection("Appointments")
                .whereEqualTo("UserID", currentUserDocumentId)
                .get()
                .addOnSuccessListener { documents ->
                    appointments.clear()  // Clear the old data
                    for (document in documents) {
                        val appointment = document.toObject(Appointment::class.java).apply {
                            documentId = document.id // Save the Firestore document ID
                        }
                        appointments.add(appointment)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Error loading appointments: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onEditAppointment(appointment: Appointment) {
        // Example: Navigate to an edit screen, passing the appointment details or ID
    }

    override fun deleteAppointment(appointment: Appointment) {
        val apptDocumentId = appointment.documentId
        if (apptDocumentId != null) {
            FirebaseFirestore.getInstance().collection("Appointments")
                .document(apptDocumentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Appointment deleted.", Toast.LENGTH_SHORT).show()
                    appointments.remove(appointment)
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("ApptFragment", "Error deleting appointment", e)
                    Toast.makeText(context, "Failed to delete appointment.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Error: Appointment ID is null.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getCurrentUserDocumentId(): String? {
        val prefs = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs?.getString("documentId", null)
    }


}