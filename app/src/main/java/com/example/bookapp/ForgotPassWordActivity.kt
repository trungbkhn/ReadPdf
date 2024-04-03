package com.example.bookapp

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityForgotPassWordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassWordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPassWordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: ProgressDialog
    private var email = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityForgotPassWordBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this)
        dialog.setTitle("Please wait")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        binding.btnSummit.setOnClickListener {
            validateData()
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }


    }

    private fun validateData() {
       email = binding.edtEmail.text.toString().trim()
        if (email.isNullOrEmpty()){
            Toast.makeText(this,"Email is empty, enter your email!",Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Pattern ", Toast.LENGTH_SHORT).show()
        } else{
            sendRequestToEmail()
        }
    }

    private fun sendRequestToEmail() {
        dialog.setMessage("Sending password reset instructions to $email")
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            //dialog.dismiss()
            Toast.makeText(this,"Sent password reset to $email!",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            dialog.dismiss()
            Toast.makeText(this,"Failed to send due to ${it.message}", Toast.LENGTH_SHORT).show()
        }

    }
}