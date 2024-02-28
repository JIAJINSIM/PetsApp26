package com.example.petsapp26

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

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

        var apptID: String? = null,
        var custID: String? = null, // Assuming this will hold the custID document ID or some identifier
        var description: Long? = null,
        var title: String? = null,
        var vetID: DocumentReference? = null
    )



    class ApptFragment : Fragment() {

        private lateinit var adapter: AppointmentAdapter
        private var appointments = mutableListOf<Appointment>()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.fragment_manageappt, container, false)
            val listView: ListView = view.findViewById(R.id.appointments_list)

            // Initialize the custom adapter with a delete callback
            adapter = AppointmentAdapter(requireContext(), appointments) { appointment ->
                deleteAppointment(appointment)
            }
            listView.adapter = adapter

            fetchAppointments()
            return view
        }


        private fun fetchAppointments() {
            val db = FirebaseFirestore.getInstance()

            db.collection("Appointments")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val apptID = document.getString("apptID") // Get apptID from the document
                        val custIDRef = document.getDocumentReference("custID")
                        val description = document.getLong("description")
                        val title = document.getString("title")
                        val vetID = document.getDocumentReference("vetID")

                        val custID =
                            custIDRef?.id // This assumes custIDRef is not null; adjust logic as needed
                        val appointment = Appointment(apptID, custID, description, title, vetID)

                        appointments.add(appointment)
                    }
                    adapter.notifyDataSetChanged() // Update adapter outside the loop for efficiency
                }

                .addOnFailureListener { exception ->
                    Log.w("ApptFragment", "Error getting documents: ", exception)
                }
        }


        private fun deleteAppointment(appointment: Appointment) {
            appointment.apptID?.let { apptID ->
                FirebaseFirestore.getInstance().collection("Appointments")
                    .document(apptID.toString()) // Convert apptID to String as Firestore document IDs are strings
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Appointment deleted.", Toast.LENGTH_SHORT).show()
                        appointments.remove(appointment)
                        adapter.notifyDataSetChanged() // Refresh the list
                    }
                    .addOnFailureListener { e ->
                        Log.e("ApptFragment", "Error deleting appointment", e)
                        Toast.makeText(context, "Failed to delete appointment.", Toast.LENGTH_SHORT).show()
                    }
            } ?: Toast.makeText(context, "Error: Appointment ID is null.", Toast.LENGTH_SHORT).show()
        }

    }



//class ApptFragment : Fragment() {
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_manageappt, container, false)
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment SettingsFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            ApptFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//}



