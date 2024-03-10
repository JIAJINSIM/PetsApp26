package com.example.petsapp26

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Retrieve the username from SharedPreferences
        val sharedPref = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val username = sharedPref?.getString("username", "User")

        // Find the TextView by ID and set the personalized welcome message
        val welcomeTextView = view.findViewById<TextView>(R.id.welcome_message)
        welcomeTextView.text = getString(R.string.welcome_message, username)

        return view
    }
}
