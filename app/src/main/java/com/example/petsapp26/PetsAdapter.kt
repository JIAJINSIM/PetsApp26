package com.example.petsapp26

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val age: Int,
    val gender: String,
    val medicalHistory: String,

)

class PetsAdapter(
    context: Context,
    private val pets: List<Pet>,
    private val itemClickListener: OnItemClickListener
) : ArrayAdapter<Pet>(context, 0, pets) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickListener {
        fun onItemClick(pet: Pet)
        fun onEditClick(pet: Pet)
        fun onDeleteClick(pet: Pet, position: Int)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_pet, parent, false)
        val pet = getItem(position)

        val nameTextView = view.findViewById<TextView>(R.id.tvPetName)
        val breedTextView = view.findViewById<TextView>(R.id.tvPetBreed)
        val ageTextView = view.findViewById<TextView>(R.id.tvPetAge)
        val genderTextView = view.findViewById<TextView>(R.id.tvPetGender)
        val medicalHistoryView = view.findViewById<TextView>(R.id.tvPetMedical)

        val btnEditPet = view.findViewById<Button>(R.id.btnEditPet)
        val btnDeletePet = view.findViewById<Button>(R.id.btnDeletePet)

        // Button actions
        view.findViewById<Button>(R.id.btnEditPet).setOnClickListener {
            pet?.let { it1 -> itemClickListener.onEditClick(it1) }
        }

        view.findViewById<Button>(R.id.btnDeletePet).setOnClickListener {
            pet?.let { it1 -> itemClickListener.onDeleteClick(it1, position) }
        }


        nameTextView.text = pet?.name
        breedTextView.text = pet?.breed
        ageTextView.text = pet?.age.toString()
        genderTextView.text = pet?.gender
        medicalHistoryView.text = pet?.medicalHistory



        view.setOnClickListener {
            pet?.let {
                itemClickListener.onItemClick(it)
            }
        }

        return view
    }
}
