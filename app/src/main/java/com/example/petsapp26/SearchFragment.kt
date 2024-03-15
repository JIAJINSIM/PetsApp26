package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private var vetClinicList: MutableList<VetClinic> = mutableListOf()
    private lateinit var vetClinicAdaptor: VetClinicAdaptor
    private lateinit var areaSpinner: Spinner
    private lateinit var ratingSpinner: Spinner
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.clinic_list)
        searchEditText = view.findViewById(R.id.search_clinic)
        searchButton = view.findViewById(R.id.search_clinic_button)
        val showFiltersButton: Button = view.findViewById(R.id.show_filters_button)
        val filtersContainer: LinearLayout = view.findViewById(R.id.filters_container)

        setupAreaSpinner(view)
        setupRatingSpinner(view)

        // Fetch and display all clinics
        fetchAllClinics()

        showFiltersButton.setOnClickListener {
            // Toggle the visibility of the filters container
            if (filtersContainer.visibility == View.GONE) {
                filtersContainer.visibility = View.VISIBLE
                showFiltersButton.text = getString(R.string.hide_filters) // Update the button text accordingly
            } else {
                filtersContainer.visibility = View.GONE
                showFiltersButton.text = getString(R.string.show_filters) // Reset button text
            }
        }

        vetClinicAdaptor = VetClinicAdaptor(requireContext(), vetClinicList)
        listView.adapter = vetClinicAdaptor

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedVeterinary = vetClinicList[position]
            val detailFragment = ClinicDetailFragment.newInstance(selectedVeterinary.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        searchButton.setOnClickListener {
            searchForClinics()
        }
    }

    private fun setupAreaSpinner(view: View) {
        val areas = arrayOf("All Areas", "Ang Mo Kio", "Yishun", "Marymount", "Other") // Add all the areas you need
        val areaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, areas)
        areaSpinner = view.findViewById(R.id.area_spinner) // This line initializes the areaSpinner
        areaSpinner.adapter = areaAdapter
    }

    private fun setupRatingSpinner(view: View) {
        val ratings = arrayOf("Any Rating", "1", "2", "3", "4", "5") // Define the rating categories you need
        val ratingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, ratings)
        ratingSpinner = view.findViewById(R.id.rating_spinner)
        ratingSpinner.adapter = ratingAdapter
    }

    private fun searchForClinics() {
        val queryText = searchEditText.text.toString().trim()
        val selectedArea = areaSpinner.selectedItem.toString()
        val selectedRating = ratingSpinner.selectedItem.toString()

        // Start building the query
        var query: Query = firestore.collection("veterinaries")

        if (queryText.isNotEmpty()) {
            val endText = queryText + "\uf8ff" // High code point character to ensure all subsequent characters are included
            query = query.whereGreaterThanOrEqualTo("name", queryText)
                .whereLessThanOrEqualTo("name", endText)
        }

        if (selectedArea != "All Areas") {
            query = query.whereEqualTo("location.area", selectedArea)
        }
        if (selectedRating != "Any Rating") {
            val ratingValue = selectedRating.toDoubleOrNull() ?: 0.0
            query = query.whereEqualTo("rating", ratingValue)
        }
        // Execute the query
        query.get()
            .addOnSuccessListener { result ->
                vetClinicList.clear()
                for (document in result) {
                    val vetClinic = document.toObject(VetClinic::class.java).apply {
                        id = document.id
                    }
                    vetClinicList.add(vetClinic)
                }
                vetClinicAdaptor.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun fetchAllClinics() {
        firestore.collection("veterinaries")
            .get()
            .addOnSuccessListener { result ->
                vetClinicList.clear()
                for (document in result) {
                    val vetClinic = document.toObject(VetClinic::class.java).apply {
                        id = document.id
                    }
                    vetClinicList.add(vetClinic)
                }
                vetClinicAdaptor.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error, possibly by showing a message to the user
            }
    }
}
