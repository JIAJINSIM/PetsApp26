package com.example.petsapp26

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [ApptFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

data class Appointment(
    var documentId: String? = null, // Local document ID, not in Firestore
    @PropertyName("Date") val Date: String? = null,
    @PropertyName("Description") val Description: String? = null,
    @PropertyName("Time") val Time: String? = null,
    @PropertyName("UserID") val UserID: String? = null,
    @PropertyName("Username") val Username: String? = null,
    @get:PropertyName("Vet Name")
    @set:PropertyName("Vet Name")
    var VetName: String? = null,
    @get:PropertyName("Veterinarian Name")
    @set:PropertyName("Veterinarian Name")
    var VeterinarianName: String? = null,
    @get:PropertyName("Veterinarian UID")
    @set:PropertyName("Veterinarian UID")
    var VeterinarianUID: String? = null
)


    class ApptFragment : Fragment(),AppointmentActionListener {



        private lateinit var adapter: AppointmentAdapter
        private var appointments = mutableListOf<Appointment>()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.fragment_manageappt, container, false)
            val listView: ListView = view.findViewById(R.id.appointments_list)

            // Initialize the custom adapter with a delete callback
            adapter = AppointmentAdapter(requireContext(), appointments, this)

            listView.adapter = adapter

            // Retrieve the current user ID from SharedPreferences
            val userId = PreferencesUtil.getUID(requireContext())
            Log.d("tag", "Test" + userId)

            fetchAppointments(userId)
            return view
        }


        private fun fetchAppointments(userID: String?) {
            val db = FirebaseFirestore.getInstance()

            // clear existing appointments before fetching new ones
            appointments.clear()

            db.collection("Appointments")
                .whereEqualTo("Veterinarian UID", userID)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val appointment = document.toObject(Appointment::class.java).apply {
                            documentId = document.id
                        }
                        appointments.add(appointment)
                    }
                    adapter.notifyDataSetChanged() // Update adapter with the new list of appointments
                }
                .addOnFailureListener { exception ->
                    Log.w("ApptFragment", "Error getting documents: ", exception)
                }
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
        override fun onEditAppointment(appointment: Appointment) {
            val bundle = Bundle().apply {
                putString("documentId", appointment.documentId)
                putString("date", appointment.Date)
                putString("time",appointment.Time)
                putString("description", appointment.Description)
            }
            navigateToEditAppointmentFragment(bundle)
        }

        private fun navigateToEditAppointmentFragment(bundle: Bundle) {
            val fragment = EditAppointmentFragment().apply {
                arguments = bundle
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }


    }

