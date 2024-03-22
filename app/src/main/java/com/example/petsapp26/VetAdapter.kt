package com.example.petsapp26

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class VetAdapter(context: Context, vets: List<Vet>, private val onScheduleClick: (Vet) -> Unit) :
    ArrayAdapter<Vet>(context, 0, vets) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_bookclinics, parent, false)
        val currentVet = getItem(position) ?: return listItemView

        val nameTextView = listItemView.findViewById<TextView>(R.id.clinic_nameList)
        val locationTextView = listItemView.findViewById<TextView>(R.id.clinic_locList)
        val ratingTextView = listItemView.findViewById<TextView>(R.id.clinic_ratingsList)
        val servicesTextView = listItemView.findViewById<TextView>(R.id.clinic_servicesList)
        val scheduleButton = listItemView.findViewById<Button>(R.id.schedule_appointment_button)

        nameTextView.text = currentVet.name
        locationTextView.text = "Area: ${currentVet.location}"
        ratingTextView.text = "Rating: ${currentVet.rating}"
        servicesTextView.text = "Services: ${currentVet.services.joinToString(", ")}"

        scheduleButton.setOnClickListener {
            val scheduleFragment = ScheduleApptFragment().apply {
                arguments = Bundle().apply {
                    putString("vetName", currentVet.name)
                    putString("vetServices", currentVet.services.joinToString(", "))
                    putString("vetRating", currentVet.rating.toString())
                }
            }
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, scheduleFragment)
                .addToBackStack(null)
                .commit()
        }


        return listItemView
    }
}
