package com.example.bookapp


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var binding: ActivityLoginBinding
private lateinit var authFirebase: FirebaseAuth
private lateinit var progressdialog: ProgressDialog

class LoginActivity : AppCompatActivity() {
    private var email = ""
    private var password = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // khai bao

        authFirebase = FirebaseAuth.getInstance()
        progressdialog = ProgressDialog(this)
        progressdialog.setTitle("Please wait...")
        progressdialog.setCanceledOnTouchOutside(false)

        binding.btnLogin.setOnClickListener {
            validateData()
        }
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgotPassWordActivity::class.java))
            finish()
        }

    }

    private fun validateData() {
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPassWord.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern... ", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password! ", Toast.LENGTH_SHORT).show()
        } else {
            logIn()
        }
    }


    private fun logIn() {
        progressdialog.setMessage("Please wait...")
        progressdialog.show()
        authFirebase.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            checkUserInfor()
        }
            .addOnFailureListener {
                progressdialog.dismiss()
                Toast.makeText(this, "Error ${it.message}", Toast.LENGTH_SHORT)
                    .show()
                authFirebase.signOut()
            }
    }

    private fun checkUserInfor() {
        progressdialog.setMessage("Please wait for checking user...")
        progressdialog.show()
        val firebaseUser = authFirebase.currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressdialog.dismiss()
                    val userType = snapshot.child("userType").value
                    if (userType == "admin") {
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                DashBoardAdminActivity::class.java
                            )
                        )
                        finish()
                    } else if (userType == "user") {
                        startActivity(Intent(this@LoginActivity, DashBoardUserActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }
}