package com.example.petsapp26

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VetStaffAdapter(private val vetStaffList: ArrayList<VetStaff>, private val context: Context) :
    RecyclerView.Adapter<VetStaffAdapter.VeterinarianViewHolder>() {

    class VeterinarianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.veterinarianName)
        val experienceTextView: TextView = itemView.findViewById(R.id.veterinarianExperience)
        val qualificationTextView: TextView = itemView.findViewById(R.id.veterinarianQualification)
        val specialisationTextView: TextView = itemView.findViewById(R.id.veterinarianSpecialisation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VeterinarianViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_vetstaff, parent, false)
        return VeterinarianViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VeterinarianViewHolder, position: Int) {
        val currentVeterinarian = vetStaffList[position]
        holder.nameTextView.text = currentVeterinarian.name
        holder.experienceTextView.text = "Experience: ${currentVeterinarian.experience}"
        holder.qualificationTextView.text = "Qualification: ${currentVeterinarian.qualification}"
        holder.specialisationTextView.text = "Specialisation: ${currentVeterinarian.specialisation}"
    }

    override fun getItemCount() = vetStaffList.size
}