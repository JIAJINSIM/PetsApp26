package com.example.petsapp26

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

        // Check if the Accessibility Service is enabled and prompt the user if not
        if (!isAccessibilityServiceEnabled(requireContext(), MyAccessibilityService::class.java)) {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Enable Accessibility Service")
                setMessage("Our app requires the accessibility service to function properly. Please enable it in the settings.")
                setPositiveButton("Go to Settings") { _, _ ->
                    // Intent to open the accessibility settings
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                setNegativeButton("Cancel", null)
                show()
            }
        }

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

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val prefString = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        prefString?.split(":")?.forEach { enabledService ->
            val expectedComponentName = ComponentName(context, service).flattenToString()
            if (expectedComponentName == enabledService) {
                return true
            }
        }
        return false
    }


}
