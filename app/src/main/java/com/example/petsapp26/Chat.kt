package com.example.petsapp26

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task


data class Message(
    @PropertyName("sender") val sender: String = "",
    @PropertyName("message") val message: String = "",
    @PropertyName("timestamp") val timestamp: Timestamp = Timestamp.now(),
    @PropertyName("receiver") val receiver: String? = null, // Add this if you are using a receiver field in Firestore
    var senderUsername: String = "" // Temporary field to hold the username once fetched
)

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
private lateinit var fusedLocationClient: FusedLocationProviderClient


class Chat : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter: ChatAdapter // Declare adapter at class level
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Update UI elements in the layout
        /*userId?.let {
            view.findViewById<TextView>(R.id.textViewUserId)?.text = it
        }*/

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView) // Initialization moved here
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatAdapter()
        recyclerView.adapter = adapter
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fetchMessages()
        view.findViewById<Button>(R.id.buttonSendMessage).setOnClickListener {
        sendMessage()
        view.findViewById<EditText>(R.id.editTextMessage).text.clear() // Clear the input field after sending

    }
        // Store location in Firestore when the chat view is created
        storelocationwhenchat()
        view.findViewById<Button>(R.id.buttonShareLocation).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            } else {
                // Permission already granted, share the location
                shareLocation()
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, proceed with sharing location
                    shareLocation()
                } else {
                    // Permission denied, show a message to the user explaining why the permission is needed
                    Toast.makeText(context, "Location permission is needed to share your location.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun shareLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission check logic and request if necessary
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1 // Get a single update of the location
            maxWaitTime = 10000 // 10 seconds
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getLocationUpdates(locationRequest)
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    startIntentSenderForResult(exception.resolution.intentSender, REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                shareLocation()
            } else {
                Toast.makeText(context, "Location not enabled", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getLocationUpdates(locationRequest: LocationRequest) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                val location = locationResult.locations.firstOrNull()
                location?.let {
                    val locationMessage = "Location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                    sendLocationMessage(locationMessage)
                } ?: run {
                    Toast.makeText(context, "Unable to get current location.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Explicitly check for permissions again
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
            } catch (e: SecurityException) {
                // Handle the security exception
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Permissions not granted, handle accordingly
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storelocationwhenchat() {
        // Check if location permissions are granted
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
            numUpdates = 1 // Only need a single update
            maxWaitTime = 10000 // Wait at most 10 seconds
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                val location = locationResult.locations.firstOrNull()
                location?.let {
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
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error storing location and user session", e)
                        }
                } ?: Log.d(TAG, "Unable to get location.")
            }
        }

        // Request location updates with the settings defined above
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }



    private fun sendLocationMessage(locationMessage: String) {
        if (locationMessage.isNotEmpty()) {
            val receiverId = arguments?.getString(ARG_PARAM1)
            val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())

            val message = hashMapOf(
                "sender" to currentUserId,
                "receiver" to receiverId,
                "message" to locationMessage,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("chats").add(message)
                .addOnSuccessListener {
                    Log.d(TAG, "Location sent successfully")
                    fetchMessages()
                    // Any additional handling after successfully sending the location
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send location", e)
                    // Handle failure
                }
        }
    }

    private fun sendMessage() {
        val editTextMessage = view?.findViewById<EditText>(R.id.editTextMessage)
        val messageText = editTextMessage?.text.toString()
        if (messageText.isNotEmpty()) {
            val receiverId = arguments?.getString(ARG_PARAM1)
            val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())

            val message = hashMapOf(
                "sender" to currentUserId,
                "receiver" to receiverId,
                "message" to messageText,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("chats").add(message)
                .addOnSuccessListener {
                    Log.d(TAG, "Message sent successfully")
                    editTextMessage?.text?.clear() // Correctly clear the text field
                    fetchMessages() // Adjusted: No need to pass adapter as it's a class variable
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send message", e)
                }
        }
    }


    private fun fetchMessages() {
        val selectedUserId = arguments?.getString(ARG_PARAM1)
        val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())

        Log.d(TAG, "currentuserID from fetchMessage:, $currentUserId")
        // Start with messages where the current user is the sender.
        firestore.collection("chats")
            .whereEqualTo("sender", currentUserId)
            .whereEqualTo("receiver", selectedUserId)
            .get()
            .addOnSuccessListener { documents1 ->
                val messages = documents1.mapNotNull { it.toObject(Message::class.java) }.toMutableList()

                // Next, fetch messages where the current user is the receiver.
                firestore.collection("chats")
                    .whereEqualTo("sender", selectedUserId)
                    .whereEqualTo("receiver", currentUserId)
                    .get()
                    .addOnSuccessListener { documents2 ->
                        messages.addAll(documents2.mapNotNull { it.toObject(Message::class.java) })

                        // Continue with username fetching and updating UI...
                        fetchUsernames(messages.map { it.sender }.toSet(), messages)
                    }
            }
    }
    private fun fetchUsernames(userIds: Set<String>, messages: MutableList<Message>) {
        val usernameMap = HashMap<String, String>()

        // Create a task list for all fetch operations
        val tasks = userIds.map { userId ->
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Unknown"
                usernameMap[userId] = username
            }
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            // Map usernames to messages
            messages.forEach { message ->
                message.senderUsername = usernameMap[message.sender] ?: "Unknown"
            }

            // Sort messages by timestamp
            messages.sortBy { it.timestamp }

            // Update adapter's data source and UI
            adapter.updateData(messages)
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    fun clearChatData() {
        adapter.clearData() // This assumes you have a method called clearData in your ChatAdapter that clears the data list and notifies the adapter.
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val REQUEST_CHECK_SETTINGS = 1001 // Add this line

        @JvmStatic
        // Adjust parameter names as necessary
        fun newInstance(selectedUserId: String): Chat {

            val fragment = Chat()
            val args = Bundle().apply {
                putString(ARG_PARAM1, selectedUserId)
                Log.d(TAG, "Select user ID receiver from companion object:, $selectedUserId")
            }
            fragment.arguments = args
            return fragment
        }
    }

}
