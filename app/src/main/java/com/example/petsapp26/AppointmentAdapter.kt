package com.example.petsapp26

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AppointmentAdapter(context: Context, appointments: List<Appointment>) :
    ArrayAdapter<Appointment>(context, 0, appointments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Check if an existing view is being reused, otherwise inflate the view
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_appt, parent, false)

        // Get the data item for this position
        val appointment = getItem(position)

        // Lookup view for data population
        val itemName = view.findViewById<TextView>(R.id.item_name)
        val itemDescription = view.findViewById<TextView>(R.id.item_description)

        // Populate the data into the template view using the data object
        itemName.text = appointment?.title
        itemDescription.text = "Description ID: ${appointment?.description}"

        // Return the completed view to render on screen
        return view
    }
}
