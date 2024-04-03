package com.example.bookapp

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bookapp.adapter.PdfProfileAdapter
import com.example.bookapp.application.MyApplication
import com.example.bookapp.databinding.ActivityProfileBinding
import com.example.bookapp.model.ModelPdf
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {


    companion object {
        const val TAG = "ProfileActivity---"
    }

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var pdfProfileAdapter: PdfProfileAdapter
    private lateinit var bookArrayList: ArrayList<ModelPdf>
    private var profileImage = ""
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var dialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        dialog = ProgressDialog(this)
        dialog.setTitle("Please wait")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        binding.rcvProfileFvB.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, EditProfileActivity::class.java))
        }
        binding.imgProfile.setOnClickListener {
            showZoomedImageDialog()
        }
        binding.tvAccountStatus.setOnClickListener {
            if (firebaseUser.isEmailVerified) {
                Toast.makeText(this, "Already verified", Toast.LENGTH_SHORT).show()
            } else {
                emailVerificationDialog()
            }
        }
        loadUserInfo()

    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Are you sure you want to send email verification instructions to your email ${firebaseUser.email}")
            .setPositiveButton("Yes") { d, e ->
                d.dismiss()
                sendEmailVerification()
            }.setNegativeButton("No") { d, e ->
                d.dismiss()
            }.show()
    }

    private fun sendEmailVerification() {
        dialog.setMessage("Sending email verification instructions to ${firebaseUser.email}")
        dialog.show()
        firebaseUser.sendEmailVerification().addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(
                this,
                "Sent email verification to ${firebaseUser.email}",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener {
            dialog.dismiss()
            Toast.makeText(
                this,
                "Can not send email verification: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showZoomedImageDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_zoom_image)

        val zoomedImageView: PhotoView = dialog.findViewById(R.id.zoomedImageView)
        val btnClose: ImageButton = dialog.findViewById(R.id.btnClose)

        // Load hình ảnh vào PhotoView
        Glide.with(this@ProfileActivity)
            .load(profileImage)
            .placeholder(R.drawable.ic_person_gray)
            .into(zoomedImageView)


        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadBookPdf() {
        bookArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.uid!!).child("Favourites")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookArrayList.clear()
                for (i in snapshot.children) {
                    val bookId = "${i.child("bookId").value}"
                    val model = ModelPdf()
                    model.id = bookId
                    bookArrayList.add(model)
                    Log.d(TAG, "Load Book Thanh Cong, BookId: $bookId!")
                }
                binding.tvFavouriteBooksCount.text = bookArrayList.size.toString()
                pdfProfileAdapter = PdfProfileAdapter(this@ProfileActivity, bookArrayList)
                binding.rcvProfileFvB.adapter = pdfProfileAdapter

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun loadUserInfo() {
        // check account verify
        if (firebaseUser.isEmailVerified){
            dialog.dismiss()
            binding.tvAccountStatus.text = "Verified"
        } else{
            dialog.dismiss()
            binding.tvAccountStatus.text = "Not verified"
        }
        Log.d(TAG, "Loading User Info...")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Loading user info succeed!")
                val email = "${snapshot.child("email").value}"
                val name = "${snapshot.child("name").value}"
                val timeStamp = "${snapshot.child("timeStamp").value}"
                val userType = "${snapshot.child("userType").value}"
                profileImage = "${snapshot.child("profileImage").value}"
                val formatDate = MyApplication.formatTimestamp(timestamp = timeStamp.toLong())
                binding.tvFullName.text = name
                binding.tvEmail.text = email
                binding.tvAccountType.text = userType
                binding.tvMemberDate.text = formatDate

                try {
                    Glide.with(this@ProfileActivity).load(profileImage)
                        .placeholder(R.drawable.ic_person_gray).into(
                            binding.imgProfile
                        )
                } catch (e: Exception) {
                    Log.e(TAG, "Fail due to ${e.message}")
                }
                loadBookPdf()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}