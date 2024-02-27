package com.example.petsapp26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {

    // Inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using the fragment_search layout
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    // Optional: Override this if you need to perform initialization
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Perform additional view setup here, like setting up search functionality
    }

    // Optional: Override other Fragment lifecycle methods as needed
    // ...
}
