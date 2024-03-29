package com.example.petsapp26

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth



import com.example.petsapp26.databinding.FragmentLoginBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import android.Manifest
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
lateinit var fusedLocationClient2: FusedLocationProviderClient

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

        binding.username.requestFocus()
        fusedLocationClient2 = LocationServices.getFusedLocationProviderClient(requireActivity())

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

                if (documents.isEmpty) {
                    // Handle case where no user is found
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in documents) {

                    val storedHashedPassword = document.getString("password")
                    val inputHashedPassword = hashPassword(password) // Hash the input password
                    //val storedPassword = document.getString("password")
                    val uid = document.getString("uid")
                    val username = document.getString("username")
                    // Inside your login success block
                    val documentId = document.id  // This is the Firestore document ID
                    val role = document.getString("role")


                    if (storedHashedPassword == inputHashedPassword) {

                        val editor = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)?.edit()
                        //editor?.putString("userID", userId)
                        editor?.putString("username", username)
                        editor?.putString("role", role)
                        editor?.putString("documentId", documentId)
                        editor?.apply()

                        println(username)
                        println(role)
                        println(documentId)
                        // storeUserRole(uid, username,document.getString("role") ?: "user")
                        // updateNavigationView(document.getString("role") ?: "user")
                        //enableNavigationDrawer()

                        // Login successful
                        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to HomeFragment


                        val role = document.getString("role") ?: "user" // Default to "user" if null
                        navigateToHome(role)
                        // storeUserRole(uid, username, role) // Store the role
                        updateNavigationView(role)
                        enableNavigationDrawer()


                        val userId = document.id
                        PreferencesUtil.storeUserIdInPreferences(userId, requireContext()) // Store user ID in preferences
                        // Fetch user document ID and log it
                        // val userId = document.id
                        // Log.d("LoginFragment", "User ID: $userId")
                        // Pass the user ID to the Contacts fragment
//                        val contactsFragment = Contacts.newInstance(userId, "")
//                        contactsFragment.receiveUserId(userId)

                        // Pass the Firestore document ID to the Contacts fragment
                        // val contactsFragment = Contacts.newInstance(documentId, "")
                        // contactsFragment.receiveUserId(documentId)

                        // Log.d("LoginFragment", "User Document ID: $documentId")


                        println("Username: $username, UID: $documentId, Password: $password")
                        // Now, request for location updates
                        requestLocationUpdates()


                        return@addOnSuccessListener
                    }
                }
                Toast.makeText(requireContext(), "Login Failed!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Login Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private var shouldStoreLocation = true
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted. Not storing location.")
            return // Early return if no permission
        }
        // Assuming you have a way to get the current user's ID or username
        val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())
        if (currentUserId == null) {
            Log.d(TAG, "No current user session found. Not storing location.")
            return // Early return if no user session
        }
        // Create location request
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // Request update every 5 seconds
            fastestInterval = 2000 // Accept updates as fast as 2 seconds
            maxWaitTime = 10000 // Wait at most 10 seconds
            Log.d(TAG, "Stuck at location request again?.")
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (!shouldStoreLocation) {
                    // If we shouldn't store the location, exit early
                    return
                }

                locationResult?.locations?.firstOrNull()?.let { location ->
                    // Include the current user's session info with the location data
                    val locationData = hashMapOf(
                        "userId" to currentUserId,
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    firestore.collection("userLocations").add(locationData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Location and user session stored successfully.")
                            shouldStoreLocation = false // Reset flag after successful storage
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error storing location and user session", e)
                        }
                } ?: Log.d(TAG, "Unable to get location.")
            }
        }

        fusedLocationClient2.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)

    }

//    private fun storeUserRole(documentId: String?, username: String?, role: String) {
//        activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)?.edit()?.apply {
//            putString("userID", documentId)
//            putString("username", username)
//            putString("userRole", role)
//            apply()
//        }
//    }

    private fun navigateToHome(role: String) {
        val fragment = when (role) {
            "admin" -> AdminHomeFragment() // Use the admin home fragment for admins
            else -> HomeFragment() // Use the regular home fragment for other roles
        }
        fragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
            commit()
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

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
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
