package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class RecordDetailFragment : Fragment() {

    private var record: Record? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the record from the arguments
        arguments?.let {
            record = it.getSerializable("record") as Record?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewrecord, container, false)

        // Use the record to populate the views
        view.findViewById<TextView>(R.id.recordID).text = "Record ID: ${record?.recordID}"
        view.findViewById<TextView>(R.id.custID).text = "Customer ID: ${record?.custID}"
//        view.findViewById<TextView>(R.id.petID).text = "Pet ID: ${record?.petID}"
        view.findViewById<TextView>(R.id.description).text = record?.description
        view.findViewById<TextView>(R.id.diagnosis).text = record?.diagnosis
        view.findViewById<TextView>(R.id.symptoms).text = record?.symptoms
        view.findViewById<TextView>(R.id.treatment).text = record?.treatment
        view.findViewById<TextView>(R.id.prescription).text = record?.prescription

        return view
    }
}
