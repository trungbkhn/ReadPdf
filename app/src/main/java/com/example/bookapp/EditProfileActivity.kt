package com.example.bookapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUri: Uri? = null
    private var name = ""
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setTitle("Please wait")
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgLogo.setOnClickListener {
            showImageAttachMenu()
        }
        binding.btnUpdate.setOnClickListener {
            name = binding.edtName.text.toString().trim()
            if (name.isNullOrEmpty()) {
                Toast.makeText(this, "Name must not empty!", Toast.LENGTH_SHORT).show()
            } else {
                if (imageUri == null) {
                    updateProfile("")
                } else {
                    uploadProfileImage()
                }
            }
        }
        loadUserInfo()
    }

    private fun uploadProfileImage() {
        progressDialog.setTitle("Uploading profile image")
        progressDialog.show()
        val filePathAndName = "Profile Images/" + firebaseAuth.currentUser!!.uid
        val ref = FirebaseStorage.getInstance().getReference(filePathAndName)

        ref.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            ref.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                updateProfile(downloadUrl)
                progressDialog.dismiss()
                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            progressDialog.dismiss()
            Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateProfile(imgUri: String) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = name
        if (imageUri != null){
            hashMap["profileImage"] = imgUri
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid)
        ref.updateChildren(hashMap).addOnSuccessListener {
            Toast.makeText(this, "Update succeed!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed update due to ${it.message}!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageAttachMenu() {
        val popMenu = PopupMenu(this, binding.imgLogo)
        popMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popMenu.show()
        popMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                pickImageCamera()
            } else if (id == 1) {
                pickImageGallery()
            }
            true
        }

    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "Image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), {
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                imageUri = data!!.data
                binding.imgLogo.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // chup
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri) //  setup anh chup
        cameraActivityResultLaucher.launch(intent) //hien thi
    }

    private val cameraActivityResultLaucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            if (result.resultCode == Activity.RESULT_OK) {

                imageUri?.let { uri -> binding.imgLogo.setImageURI(uri)
                    Log.d("Load anh vao uri-- ","uri = ${uri.toString()}")}
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        })

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(ProfileActivity.TAG, "Loading user info succeed!")
                val name = "${snapshot.child("name").value}"
                val timeStamp = "${snapshot.child("timeStamp").value}"
                val profileImage = "${snapshot.child("profileImage").value}"
                binding.edtName.setText(name)

                try {
                    Glide.with(this@EditProfileActivity).load(profileImage)
                        .placeholder(R.drawable.ic_person_gray).into(
                            binding.imgLogo
                        )
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}