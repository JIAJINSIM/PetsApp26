package com.example.petsapp26

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class VetClinicAdaptor(
    private val context: Context,
    private val veterinaries: List<VetClinic>
) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = veterinaries.size

    override fun getItem(position: Int): Any = veterinaries[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: inflater.inflate(R.layout.list_item_vetclinics, parent, false)
        val vetClinic = getItem(position) as VetClinic

        view.findViewById<TextView>(R.id.clinic_name).text = vetClinic.name
        view.findViewById<TextView>(R.id.clinic_address).text = vetClinic.location.address

        return view
    }
}