package com.example.petsapp26

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class AdminHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
// Find the button by ID and set a click listener
        view.findViewById<Button>(R.id.addnewrecord_button).setOnClickListener {
            navigateToAddNewRecordFragment()
        }
        // Retrieve the username from SharedPreferences
        val sharedPref = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val username = sharedPref?.getString("username", "User")

        // Find the TextView by ID and set the personalized welcome message
        val welcomeTextView = view.findViewById<TextView>(R.id.welcome_message)
        welcomeTextView.text = getString(R.string.welcome_message, username)

        return view
    }

    private fun navigateToAddNewRecordFragment() {
        // Create an instance of the fragment you want to navigate to
        val addRecFragment = AddNewRecordFragment() // Make sure you have this fragment created

        // Perform the fragment transaction to replace the current fragment with the new one
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, addRecFragment) // Use the correct container ID
            .addToBackStack(null) // Add this transaction to the back stack
            .commit()
    }

}
