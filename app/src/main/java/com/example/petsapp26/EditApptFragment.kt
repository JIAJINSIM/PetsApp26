package com.example.petsapp26

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petsapp26.databinding.FragmentEditapptBinding // Import generated binding class
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditAppointmentFragment : Fragment() {

    private var _binding: FragmentEditapptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditapptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("apptID")?.let { apptID ->
            preloadAppointmentDetails(apptID)
        }

        // Setup click listener for editTextDate to show DatePickerDialog
        _binding?.editTextDate?.setOnClickListener {
            showDatePickerDialog()
        }

        // Time picker dialog
        _binding?.editTextTime?.setOnClickListener {
            showTimePickerDialog()
        }

        binding.buttonSaveAppointment.setOnClickListener {
            saveAppointmentChanges()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            // Use the selected date
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, monthOfYear, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            _binding?.editTextDate?.setText(dateFormat.format(selectedDate.time))
        }, year, month, day)

        dpd.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            // Format and set the selected time
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            _binding?.editTextTime?.setText(timeFormat.format(calendar.time))
        }, hour, minute, true) // 'true' for 24-hour time

        tpd.show()
    }

    private fun preloadAppointmentDetails(apptID: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Appointments").document(apptID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Extracting fields from the document
                    val custIDRef = documentSnapshot.getDocumentReference("custID")
                    val date = documentSnapshot.getTimestamp("date")?.toDate()
                    val description = documentSnapshot.getString("description")
                    val title = documentSnapshot.getString("title")
                    val vetID = documentSnapshot.getDocumentReference("vetID")

                    // Now safe to format Date
                    val formattedDate = date?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                    } ?: "No Date" // Handle null case

                    val formattedTime = date?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                    } ?: "No Time" // Handle null case

                    // Handling DocumentReference fields like 'custIDRef' and 'vetID'
                    custIDRef?.id?.let { custID ->
                        // Preload data into your UI components here
                        view?.findViewById<EditText>(R.id.editTextDescription)?.setText(description ?: "")

                        view?.findViewById<TextView>(R.id.textCustomerID)?.text = custID
                        view?.findViewById<TextView>(R.id.editTextDate)?.text = formattedDate
                        view?.findViewById<TextView>(R.id.editTextTime)?.text = formattedTime
                    }

                } else {
                    Log.d("EditAppointmentFragment", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditAppointmentFragment", "Error getting document: ", exception)
            }
    }

    private fun saveAppointmentChanges() {
        val apptID = arguments?.getString("apptID") ?: return

        // Collect data from UI components
        val updatedDate = binding.editTextDate.text.toString() // Assuming you've formatted this as "yyyy-MM-dd"
        val updatedTime = binding.editTextTime.text.toString() // Assuming you've formatted this as "HH:mm"
        val updatedDescription = binding.editTextDescription.text.toString()
        // Add more fields as needed

        // Combine date and time into a Timestamp if necessary
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val combinedDateTime = dateTimeFormat.parse("$updatedDate $updatedTime") ?: return
        val updatedTimestamp = Timestamp(combinedDateTime)

        // Prepare data to update
        val updatedData = hashMapOf<String, Any>(
            "date" to updatedTimestamp,
            "description" to updatedDescription
            // Add other fields as necessary
        )

        // Update Firestore document
        val db = FirebaseFirestore.getInstance()
        db.collection("Appointments").document(apptID)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(context, "Appointment updated successfully.", Toast.LENGTH_SHORT).show()
                // Handle navigation or UI updates as necessary
            }
            .addOnFailureListener { e ->
                Log.w("EditAppointmentFragment", "Error updating document", e)
                Toast.makeText(context, "Error updating appointment.", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
