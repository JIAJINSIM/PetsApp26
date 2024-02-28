package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private var vetClinicList: MutableList<VetClinic> = mutableListOf()
    private lateinit var vetClinicAdaptor: VetClinicAdaptor
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.clinic_list)
        searchEditText = view.findViewById(R.id.search_clinic)
        searchButton = view.findViewById(R.id.search_clinic_button)

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
            val queryText = searchEditText.text.toString().trim()
            if (queryText.isNotEmpty()) {
                searchForClinics(queryText)
            }
        }
    }

    private fun searchForClinics(query: String) {
        firestore.collection("veterinaries")
            .whereEqualTo("name", query)
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
                // Handle error
            }
    }
}
