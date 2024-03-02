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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.FileOutputStream
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.IOException

class Profile : Fragment() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var documentIdTextView: TextView
    private var firestoreDocumentId: String? = null

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
                        saveImageToInternalStorage(imageUri, userId)
                    }
                }
                PICK_PET_IMAGE_REQUEST -> {
                    var imageUri: Uri = data.data!!
                    firestoreDocumentId?.let { userId ->
                        savePetImageToInternalStorage(imageUri, userId)
                    }
                }
            }

        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri, userId: String) {
        val inputStream = activity?.contentResolver?.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val directory = ContextWrapper(activity).getDir("profile", Context.MODE_PRIVATE)
        val mypath = File(directory, "${userId}_profilePic.jpg")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Toast.makeText(activity, "Image Saved to Internal Storage", Toast.LENGTH_SHORT).show()
    }

    private fun savePetImageToInternalStorage(imageUri: Uri, userId: String) {
        val inputStream = activity?.contentResolver?.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val timestamp = System.currentTimeMillis()
        val directory = ContextWrapper(activity).getDir("petDetails", Context.MODE_PRIVATE)

        val fileName = "${userId}_pet_$timestamp.jpg"
        val mypath = File(directory, fileName)

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Toast.makeText(activity, "Pet Image Saved to Internal Storage", Toast.LENGTH_SHORT).show()

        // After saving, reload the RecyclerView to display the new image
        loadAndDisplayPetImages(userId)
    }
    private fun loadUserProfile(documentId: String) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(documentId)

        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("username") ?: "Unknown"
                    val imageUrl = documentSnapshot.getString("profileImageUrl")

                    usernameTextView.text = "$username"
                    // documentIdTextView.text = "Document ID: $documentId"

                    imageUrl?.let { url ->
                        Glide.with(this).load(url).into(profileImageView)
                    }
                } else {
                    Toast.makeText(context, "Profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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

//    private fun loadAndDisplayPetImages(userId: String) {
//        // Make sure the directory name is the same as the one used in savePetImageToInternalStorage
//        val directory = ContextWrapper(activity).getDir("petDetails", Context.MODE_PRIVATE)
//        val petImageFiles = directory.listFiles()?.filter { it.name.startsWith(userId) }?.toMutableList() ?: mutableListOf()
//        val petImages = petImageFiles.map { BitmapFactory.decodeFile(it.absolutePath) }.toMutableList()
//
//
//        petImageFiles?.forEach { file ->
//            if (file.name.startsWith(userId)) {
//                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
//                petImages.add(bitmap)
//            }
//        }
//
//        petImageAdapter = PetImageAdapter(petImages) { position ->
//            deleteImage(position)
//
//        }
//
//        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvPetImages)
//        recyclerView?.adapter = petImageAdapter
//        recyclerView?.layoutManager = GridLayoutManager(context, 3)
//    }

    private fun loadAndDisplayPetImages(userId: String) {
        val directory = ContextWrapper(requireActivity()).getDir("petDetails", Context.MODE_PRIVATE)
        val petImageFiles = directory.listFiles()?.filter { it.name.startsWith(userId) }?.toList() ?: listOf()
        val newPetImages = petImageFiles.map { BitmapFactory.decodeFile(it.absolutePath) }.toMutableList()

        // Update the adapter's dataset
        petImageAdapter.updatePetImages(newPetImages)
    }

    private fun deleteImage(position: Int) {

        val directory = ContextWrapper(activity).getDir("petDetails", Context.MODE_PRIVATE)
        val files = directory.listFiles()

        // Check if the position is valid
        if (position >= 0 && position < files.size) {
            val fileToDelete = files[position]
            if (fileToDelete.delete()) {
                petImageAdapter.removeAt(position) // Call the new removeAt function
            } else {
                Toast.makeText(activity, "Unable to delete image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    class PetImageAdapter(
        private var petImages: MutableList<Bitmap>,
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
            holder.petImageView.setImageBitmap(petImages[position])
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

        fun updatePetImages(newPetImages: MutableList<Bitmap>) {
            petImages.clear()
            petImages.addAll(newPetImages)
            notifyDataSetChanged()
        }
    }

}