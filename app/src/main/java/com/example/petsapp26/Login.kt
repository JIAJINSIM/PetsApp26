package com.example.petsapp26

import android.content.Intent
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.example.petsapp26.databinding.FragmentLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var loginSuccessListener: OnLoginSuccessListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val registerLink = binding.registerLink

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            login(username, password)
        }

        registerLink.setOnClickListener {
            // Perform fragment transaction to show RegisterUser fragment
            fragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, RegisterUser())
                ?.addToBackStack(null)
                ?.commit()
        }


    }

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private fun login(username: String, password: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val storedPassword = document.getString("password")
                    if (storedPassword == password) {
                        // Login successful
                        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        val role = document.getString("role") ?: "user" // Default to "user" if null
                        storeUserRole(role) // Store the role
                        updateNavigationView(role)
                        enableNavigationDrawer()
                        return@addOnSuccessListener
                    }
                }
                Toast.makeText(requireContext(), "Login Failed!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Login Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
//    fun onRegisterLinkClick(view: View) {
//        // Check if we are attached to an activity, as we need the fragmentManager from it
//        if (isAdded) {
//            activity?.supportFragmentManager?.beginTransaction()
//                ?.replace(R.id.fragment_container, RegisterUser())
//                ?.addToBackStack(null) // Add this transaction to the back stack
//                ?.commit()
//        }
//    }


    private fun storeUserRole(role: String) {
        activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)?.edit()?.apply {
            putString("userRole", role)
            apply()
        }
    }


    private fun enableNavigationDrawer() {
        val mainActivity = activity as? MainActivity
        mainActivity?.enableNavigationDrawer()
    }



    private fun updateNavigationView(userRole: String?) {
        val mainActivity = activity as? MainActivity
        mainActivity?.updateNavigationView(userRole)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*class Login : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var loginSuccessListener: OnLoginSuccessListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val storedPassword = document.getString("password")
                    if (storedPassword == password) {
                        // Passwords match, login successful
                        // Navigate to the next screen or perform desired action
                        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        //enableNavigationDrawer() // Enable navigation drawer after successful login
                        return@addOnSuccessListener
                    }
                }
                // No user found with the provided username or incorrect password
                // Handle login failure
                Toast.makeText(requireContext(), "Login Failed!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Toast.makeText(requireContext(), "Login Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enableNavigationDrawer() {
        val mainActivity = activity as? MainActivity
        mainActivity?.enableNavigationDrawer()
    }


}*/