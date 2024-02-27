package com.example.petsapp26

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class RecordAdapter(context: Context, records: List<Record>) :
    ArrayAdapter<Record>(context, 0, records) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Check if an existing view is being reused, otherwise inflate the view
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_record, parent, false)

        // Get the data item for this position
        val record = getItem(position)

        // Lookup view for data population
        val itemUserID = view.findViewById<TextView>(R.id.item_userID)
        val itemRecordID = view.findViewById<TextView>(R.id.item_recordID)

        // Populate the data into the template view using the data object
        itemUserID.text = record?.userID
        itemRecordID.text = "Record ID: ${record?.recordID}"

        // Return the completed view to render on screen
        return view
    }
}
