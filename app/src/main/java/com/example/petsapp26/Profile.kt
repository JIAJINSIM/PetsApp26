package com.example.petsapp26

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.FileOutputStream
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException

class Profile : Fragment() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var documentIdTextView: TextView
    private var firestoreDocumentId: String? = null

    private val storageReference = FirebaseStorage.getInstance().reference
    private lateinit var petImageAdapter: PetImageAdapter

    private val PICK_IMAGE_REQUEST = 1
    private val PICK_PET_IMAGE_REQUEST = 2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profileImage)
        usernameTextView = view.findViewById(R.id.tvUsername)

        // documentIdTextView = view.findViewById(R.id.tvUID)
        view.findViewById<Button>(R.id.changeProfileImageButton).setOnClickListener {
            openImageChooser()
        }

        view.findViewById<Button>(R.id.addPetImageButton).setOnClickListener {
            openPetImageChooser()
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences =
            activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        firestoreDocumentId = sharedPreferences?.getString("documentId", null)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d("ProfileFragment", "User is signed in with UID: ${user.uid}")
        } else {
            Log.d("ProfileFragment", "No user is signed in.")
        }

        val username = sharedPreferences?.getString("username", "No Username")
        val documentId = sharedPreferences?.getString("documentID", "No ID")

        // Initialize the adapter with an empty list first
        petImageAdapter = PetImageAdapter(mutableListOf()) { position ->
            deleteImage(position)
        }

        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvPetImages)
        recyclerView?.adapter = petImageAdapter
        recyclerView?.layoutManager = GridLayoutManager(context, 3)

        usernameTextView.text = "Username: $username"
        // documentIdTextView.text = "Document ID: $documentId"

        firestoreDocumentId?.let {
            if (it != "No ID") {
                loadUserProfile(it)
                loadImageFromInternalStorage(it)
                loadAndDisplayPetImages(it)
            } else {
                Toast.makeText(context, "Please log in to view profile details", Toast.LENGTH_SHORT)
                    .show()
            }

        }


        view.findViewById<Button>(R.id.changeProfileDetails).setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, PetProfileFragment())
            transaction.addToBackStack(null)
            transaction?.commit()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openPetImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PET_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when(requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri: Uri = data.data!!
                    profileImageView.setImageURI(imageUri)

                    firestoreDocumentId?.let { userId ->
                        Log.d("ProfileFragment", "Authenticated user ID: $userId")
                        uploadImageToFirebaseStorage(imageUri, userId, "profilePic.jpg", false)
                    }
                }
                PICK_PET_IMAGE_REQUEST -> {
                    var imageUri: Uri = data.data!!
                    val timestamp = System.currentTimeMillis()
                    firestoreDocumentId?.let { userId ->
                        Log.d("ProfileFragment", "Authenticated user ID: $userId")
                        uploadImageToFirebaseStorage(imageUri, userId, "pet_$timestamp.jpg", true)
                    }
                }
            }

        }
    }

    // New method to upload image to Firebase Storage
    private fun uploadImageToFirebaseStorage(imageUri: Uri, userId: String, fileName: String, isPetImage: Boolean) {
        val imageRef = storageReference.child("users/$userId/$fileName")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    if (isPetImage) {
                        updatePetImages(uri.toString(), userId)
                    } else {
                        updateImageUrl(uri.toString(), userId)
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Upload failed", exception)
                Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to update the image URL in the user's profile (this may require adjusting depending on your app's Firestore structure)
    private fun updateImageUrl(imageUrl: String, userId: String) {
        // Assuming 'users' is a collection of user profiles
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userDocRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                // Optionally, load the new image into the ImageView using Glide
                Glide.with(this).load(imageUrl).into(profileImageView)
                Toast.makeText(context, "Profile image updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Call this method after uploading pet image
    private fun updatePetImages(imageUrl: String, userId: String) {
        Glide.with(this).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                val petImage = PetImage(resource, imageUrl)
                // Add to your adapter's data set
                petImageAdapter.addImage(petImage)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // This is called when ImageView is cleared, if you are using placeholders, etc.
            }
        })
    }


    private fun loadUserProfile(documentId: String) {
        Log.d("ProfileFragment", "Loading user profile for document ID: $documentId")

        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(documentId)

        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("username") ?: "Unknown"
                    val imageUrl = documentSnapshot.getString("profileImageUrl")

                    Log.d("ProfileFragment", "User profile loaded successfully. Username: $username, Image URL: $imageUrl")

                    usernameTextView.text = "$username"
                    // documentIdTextView.text = "Document ID: $documentId"

                    imageUrl?.let { url ->
                        Glide.with(this).load(url).into(profileImageView)
                    }
                } else {
                    Log.d("ProfileFragment", "Profile not found for document ID: $documentId")
                    Toast.makeText(context, "Profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error loading profile: ${e.message}", e)
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadImageFromInternalStorage(userId: String) {
        val directory = ContextWrapper(activity).getDir("profile", Context.MODE_PRIVATE)
        val file = File(directory, "${userId}_profilePic.jpg")
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            profileImageView.setImageBitmap(bitmap)
        }
    }


    private fun loadAndDisplayPetImages(userId: String) {
        Log.d("ProfileFragment", "Loading and displaying pet images for user ID: $userId")

        // Construct the storage reference for the user's images folder
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userId")

        // List all items (images) in the user's images folder
        storageRef.listAll().addOnSuccessListener { listResult ->
            val images = mutableListOf<PetImage>()
            // Iterate through the items (images) in the folder
            listResult.items.forEachIndexed { index, imageRef ->
                // Get the download URL for each image
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Load the image using Glide
                    Glide.with(this@Profile).asBitmap().load(uri).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            // Log that the image was successfully loaded
                            Log.d("ProfileFragment", "Pet image loaded successfully from URL: $uri")

                            val petImage = PetImage(resource, uri.toString())
                            images.add(petImage)

                            // Check if all images have been loaded
                            if (index == listResult.items.size - 1) {
                                // All images have been loaded, update the adapter with the list of images
                                petImageAdapter.updatePetImages(images)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Log that the image load was cleared
                            Log.d("ProfileFragment", "Pet image load cleared for URL: $uri")
                        }
                    })
                }
            }
        }.addOnFailureListener { exception ->
            // Log any errors that occur while listing images
            Log.e("ProfileFragment", "Error listing pet images: ${exception.message}", exception)
        }
    }




//    private fun deleteImage(position: Int) {
//
//        val directory = ContextWrapper(activity).getDir("petDetails", Context.MODE_PRIVATE)
//        val files = directory.listFiles()
//
//        // Check if the position is valid
//        if (position >= 0 && position < files.size) {
//            val fileToDelete = files[position]
//            if (fileToDelete.delete()) {
//                petImageAdapter.removeAt(position) // Call the new removeAt function
//            } else {
//                Toast.makeText(activity, "Unable to delete image", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


    private fun deleteImage(position: Int) {
        if (position < 0 || position >= petImageAdapter.itemCount) {
            Toast.makeText(activity, "Invalid image position", Toast.LENGTH_SHORT).show()
            return
        }

        val imagePath = petImageAdapter.getImagePathAt(position)
        val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath)

        imageRef.delete().addOnSuccessListener {
            // Image successfully deleted from Firebase Storage
            petImageAdapter.removeAt(position)
            Toast.makeText(activity, "Image deleted successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(activity, "Failed to delete image: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    data class PetImage(val bitmap: Bitmap, val imagePath: String)


    class PetImageAdapter(
        private var petImages: MutableList<PetImage>,
        private val onDeleteClick: (Int) -> Unit) :
        RecyclerView.Adapter<PetImageAdapter.PetImageViewHolder>() {

        inner class PetImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val petImageView: ImageView = view.findViewById(R.id.petImageView) // Replace with your actual ImageView id
            val deleteImageView: ImageView = view.findViewById(R.id.deleteImageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet_image, parent, false)
            return PetImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: PetImageViewHolder, position: Int) {
            val petImage = petImages[position]
            holder.petImageView.setImageBitmap(petImage.bitmap) // Use the bitmap property from PetImage
            holder.deleteImageView.setOnClickListener {
                onDeleteClick(position)
            }
        }

        override fun getItemCount(): Int {
            return petImages.size
        }

        fun removeAt(position: Int) {
            if (position >= 0 && position < petImages.size) {
                petImages.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, petImages.size) // Update the positions of remaining items
            }
        }

        fun getImagePathAt(position: Int): String {
            return petImages[position].imagePath
        }


        fun updatePetImages(newPetImages: MutableList<PetImage>) {
            petImages.clear()
            petImages.addAll(newPetImages)
            notifyDataSetChanged()
        }

        fun addImage(petImage: PetImage) {
            petImages.add(petImage)
            notifyItemInserted(petImages.size - 1)
        }
    }


}