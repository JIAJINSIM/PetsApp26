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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query


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
        recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd= true }
        adapter = ChatAdapter()
        recyclerView.adapter = adapter
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fetchMessagesAndUsernames()
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
    private var shouldStoreLocation = true
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

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)

    }
    // Call this method each time the user activates the chat function to reset the flag
    private fun activateChatFunction() {
        shouldStoreLocation = true // Allow storing location again
        storelocationwhenchat() // Optionally, call this to immediately try storing location upon activation
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
                    fetchMessagesAndUsernames()
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
                    fetchMessagesAndUsernames() // Adjusted: No need to pass adapter as it's a class variable
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send message", e)
                }
        }
    }


    private fun fetchMessagesAndUsernames() {
        val selectedUserId = arguments?.getString(ARG_PARAM1)
        val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())

        if (currentUserId == null || selectedUserId == null) {
            Log.w(TAG, "Current or selected user ID is null.")
            return
        }

        // This set will hold all user IDs for which we need to fetch usernames.
        val userIdsToFetch = mutableSetOf(currentUserId, selectedUserId)

        // Map to store usernames for user IDs.
        val usernamesMap = mutableMapOf<String, String>()

        // Initially fetch usernames to ensure we have them before messages start coming in.
        fetchUsernames(userIdsToFetch) { fetchedUsernamesMap ->
            usernamesMap.putAll(fetchedUsernamesMap)

            // After fetching usernames, start listening for messages.
            listenForMessages(currentUserId, selectedUserId, usernamesMap)
        }
    }

    private fun fetchUsernames(userIds: Set<String>, onComplete: (Map<String, String>) -> Unit) {
        val tasks = userIds.map { userId ->
            firestore.collection("users").document(userId).get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
            val usernamesMap: Map<String, String> = documents.mapNotNull { document ->
                val userId = document.id
                val username = document.getString("username") ?: "Unknown" // Provide a default value for null usernames
                userId to username
            }.toMap()
            onComplete(usernamesMap)
        }
    }


    private fun listenForMessages(currentUserId: String, selectedUserId: String, usernamesMap: MutableMap<String, String>) {
        val messages = mutableListOf<Message>()

        firestore.collection("chats")
            .whereIn("sender", listOf(currentUserId, selectedUserId))
            .whereIn("receiver", listOf(currentUserId, selectedUserId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val documentChanges = snapshots?.documentChanges ?: return@addSnapshotListener
                for (change in documentChanges) {
                    val message = change.document.toObject(Message::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            // Assign fetched usernames
                            message.senderUsername = usernamesMap[message.sender] ?: "Unknown"
                            messages.add(message)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            // Handle modifications if necessary
                        }
                        DocumentChange.Type.REMOVED -> {
                            // Handle removals if necessary
                        }
                    }
                }

                // Sort and update UI
                messages.sortBy { it.timestamp }
                adapter.updateData(messages)
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }
    }



    private fun fetchUsernamesAndUpdate(messages: List<Message>) {
        val userIds = messages.map { it.sender }.toSet()

        Tasks.whenAllSuccess<DocumentSnapshot>(userIds.map { userId ->
            firestore.collection("users").document(userId).get()
        }).addOnSuccessListener { documents ->
            val usernameMap = documents.mapNotNull { it.toObject(User::class.java) }.associateBy { it.userId }.mapValues { it.value.username }

            val updatedMessages = messages.map { message ->
                message.apply { senderUsername = usernameMap[sender] ?: "Unknown" }
            }.sortedBy { it.timestamp }

            adapter.updateData(updatedMessages)
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    data class User(
        val userId: String = "",
        val username: String = "",
        // Include other fields as necessary
    )


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
