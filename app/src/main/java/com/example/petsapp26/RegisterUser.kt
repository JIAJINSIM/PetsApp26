package com.example.petsapp26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore



class RegisterUser : ComponentActivity() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private fun performRegistration(username: String, password: String, role: String, email: String) {
        val usersCollection = firestore.collection("users")
        val newDocumentRef = usersCollection.document()

        val userData = hashMapOf(
            "username" to username,
            "password" to password,
            "role" to role,
            "email" to email
        )

        newDocumentRef.set(userData)
            .addOnSuccessListener {
                // Registration successful
                // Handle success scenarios here
                showToast("Registration successful!")
            }
            .addOnFailureListener { e ->
                // Handle errors here
                showToast("Error during registration: $e")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
