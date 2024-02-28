package com.example.petsapp26

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class AppointmentAdapter(context: Context, private val appointments: List<Appointment>, private val onDelete: (Appointment) -> Unit) :
    ArrayAdapter<Appointment>(context, 0, appointments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_appt, parent, false)
        val appointment = getItem(position)

        val itemName = view.findViewById<TextView>(R.id.item_name)
        val itemDescription = view.findViewById<TextView>(R.id.item_description)
        val deleteButton = view.findViewById<Button>(R.id.delete_appointment_button)

        itemName.text = appointment?.title
        itemDescription.text = "Description ID: ${appointment?.description}"

        deleteButton.setOnClickListener {
            appointment?.let { it1 -> onDelete(it1) }
        }

        return view
    }
}
