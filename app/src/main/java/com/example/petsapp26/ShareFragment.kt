package com.example.petsapp26

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

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
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_READ_CONTACTS)
            } else {
                // Permission has already been granted, open contacts
                openContactsBook()
            }
        }

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, open contacts
                    openContactsBook()
                } else {
                    // Permission denied, show a message to the user.
                    Toast.makeText(context, "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
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
                val idCursor = requireActivity().contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID), null, null, null)
                if (idCursor?.moveToFirst() == true) {
                    val contactId = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID))
                    idCursor.close()

                    // Use the contact ID to query for phone number
                    val phoneCursor = requireActivity().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null)

                    if (phoneCursor?.moveToFirst() == true) {
                        val number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        phoneCursor.close()

                        // Now that we have the number, we can use it to open the messaging app
                        sendDefaultMessageTo(number)
                    } else {
                        Toast.makeText(context, "Failed to get contact number", Toast.LENGTH_SHORT).show()
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
        val defaultMessage = "Check out PetsApp26! It's a great app for connecting with vets and managing your pet's health."

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")  // Ensures only SMS apps respond
            putExtra("sms_body", defaultMessage)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No messaging app found to send an SMS.", Toast.LENGTH_SHORT).show()
        }
    }




}
