package com.example.petsapp26

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

data class Vet(
    val name: String = "",
    val location: String = "", // Only area field
    val rating: Double = 0.0,
    val services: List<String> = emptyList()
)

class AddApptFragment : Fragment() {

    // Assuming you have a ListView in your fragment's layout with this ID
    private lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_addappt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView: ListView = view.findViewById(R.id.clinics_list_view)
        fetchVets()
    }


    fun fetchVets() {
        val db = FirebaseFirestore.getInstance()
        val vetsList = mutableListOf<Vet>()
        db.collection("veterinaries").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val locationMap = document.get("location") as Map<String, String>? // Firestore returns a Map
                    val area = locationMap?.get("area") ?: ""
                    val rating = document.getDouble("rating") ?: 0.0
                    val services = document.get("services") as List<String>? ?: emptyList()
                    val vet = Vet(name, area, rating, services)
                    vetsList.add(vet)
                }
                // Now that we have our list, set it to the adapter
                updateListView(vetsList)
            }
            .addOnFailureListener { exception ->
                Log.d("fetchVets", "Error getting documents: ", exception)
            }
    }

    class VetAdapter(context: Context, vets: List<Vet>) :
        ArrayAdapter<Vet>(context, 0, vets) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItemView = convertView
            if (listItemView == null) {
                listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_bookclinics, parent, false)
            }

            val currentVet = getItem(position)

            val nameTextView = listItemView?.findViewById<TextView>(R.id.clinic_nameList)
            val locationTextView = listItemView?.findViewById<TextView>(R.id.clinic_locList)
            val ratingTextView = listItemView?.findViewById<TextView>(R.id.clinic_ratingsList)
            val servicesTextView = listItemView?.findViewById<TextView>(R.id.clinic_servicesList)

            nameTextView?.text = currentVet?.name
            locationTextView?.text = "Area: ${currentVet?.location}"
            ratingTextView?.text = "Rating: ${currentVet?.rating}"
            servicesTextView?.text = "Services: ${currentVet?.services?.joinToString(", ")}"

            return listItemView!!
        }
    }

    fun updateListView(vetsList: List<Vet>) {
        val adapter = VetAdapter(requireContext(), vetsList)
        val listView: ListView? = view?.findViewById(R.id.clinics_list_view)
        listView!!.adapter = adapter
    }


}

// Assuming VetClinicAdapter is similar to the custom adapter described earlier
