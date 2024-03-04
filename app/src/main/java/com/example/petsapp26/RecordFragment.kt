package com.example.petsapp26

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [ApptFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

data class Record(
    var recordID: String? = null,
    var custID: String? = null,
    var description: String? = null,
    var diagnosis: String? = null,
    var petID: String? = null,
    var prescription: String? = null,
    var symptoms: String? = null,
    var treatment: String? = null,
    var apptID: String? = null

): Serializable



class RecordFragment : Fragment() {

    private lateinit var adapter: RecordAdapter
    private var records = mutableListOf<Record>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        val listView: ListView = view.findViewById(R.id.records_list)

        // Initialize the custom adapter
        adapter = RecordAdapter(requireContext(), records)
        listView.adapter = adapter

        // Set the item click listener
        listView.setOnItemClickListener { _, _, position, _ ->
            // Get the selected record
            val selectedRecord = records[position]

            // Create new fragment and pass the selected record using a Bundle
            val recordDetailFragment = RecordDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("record", selectedRecord) // Make sure Record class implements Serializable
                }
            }

            // Perform the fragment transaction
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, recordDetailFragment)
                addToBackStack(null)  // Add transaction to the back stack
                commit()
            }
        }

        // Set up the button click listener
        val addButton: Button = view.findViewById(R.id.add_records_button)
        addButton.setOnClickListener {
            // Navigate to AddNewRecordFragment
            val addNewRecordFragment = AddNewRecordFragment()
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, addNewRecordFragment)
                addToBackStack(null) // Adds the transaction to the back stack
                commit()
            }
        }

        fetchRecords()

        return view
    }

    private fun fetchRecords() {
        val db = FirebaseFirestore.getInstance()

        // clear existing records before fetching new ones
        records.clear()

        db.collection("Records")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val custIDRef = document.getDocumentReference("custID")
                    val recordID = document.getString("recordID")
                    val petIDRef = document.getDocumentReference("petID")

                    val description = document.getString("description")
                    val diagnosis = document.getString("diagnosis")
                    val prescription = document.getString("prescription")
                    val symptoms = document.getString("symptoms")
                    val treatment = document.getString("treatment")
//                      apptID = document.getString("apptID")

//                    custIDRef?.get()?.addOnSuccessListener { custDoc ->
//                        val custID = custDoc.id


                        val record = Record(recordID, custIDRef?.id,description, diagnosis,petIDRef?.id, prescription,symptoms,treatment )

                        records.add(record)

                        // Notify the adapter of the data change on the UI thread
                        adapter.notifyDataSetChanged()
                    }
                }
            }
//            .addOnFailureListener { exception ->
//                Log.w("RecordFragment", "Error getting documents: ", exception)
//            }
    }



