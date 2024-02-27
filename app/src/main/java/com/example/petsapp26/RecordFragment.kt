package com.example.petsapp26

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
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

data class Record(
    var recordID: Long? = null,
    var userID: String? = null,
)



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

        fetchRecords()

        return view
    }

    private fun fetchRecords() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Records")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val custIDRef = document.getDocumentReference("UserID")
                    val recordID = document.getLong("RecordID")

                    custIDRef?.get()?.addOnSuccessListener { custDoc ->
                        val userID = custDoc.id
                        val record = Record(recordID, userID)

                        records.add(record)

                        // Notify the adapter of the data change on the UI thread
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("RecordFragment", "Error getting documents: ", exception)
            }
    }
}


