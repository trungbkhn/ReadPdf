package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private lateinit var binding: ActivityRegisterBinding
private lateinit var progressdialog: ProgressDialog
private lateinit var authFirebase: FirebaseAuth
class RegisterActivity : AppCompatActivity() {
    private var email = ""
    private var name = ""
    private var password = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // khai bao

        authFirebase = FirebaseAuth.getInstance()
        progressdialog = ProgressDialog(this)
        progressdialog.setTitle("Please wait")
        progressdialog.setCanceledOnTouchOutside(false)
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            validateData()
        }



    }


    private fun validateData() {
        email = binding.edtEmailRes.text.toString().trim()
        name = binding.edtNameRes.text.toString().trim()
        password = binding.edtPassWordRes.text.toString().trim()
        val cpassword = binding.edtCpassWordRes.text.toString().trim()

        //validate
        if (name.isEmpty()) {
            Toast.makeText(this, "Please Enter Your Name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Format!", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter your password! ", Toast.LENGTH_SHORT).show()
        } else if (password != cpassword) {
            Toast.makeText(this, "Password doesn't match... ", Toast.LENGTH_SHORT).show()
        } else {
            createAccount()
        }
    }

    private fun createAccount() {
        progressdialog.setMessage("Please waiting ...")
        progressdialog.show()
        authFirebase.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            updateUserInfor()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_SHORT)
                .show()
            progressdialog.dismiss()
        }
    }

    private fun updateUserInfor() {
        val uid = authFirebase.uid
        progressdialog.setMessage("Saving user infor...")
        //timestamp
        val timeStamp = System.currentTimeMillis()
        //hashmap
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timeStamp"] = timeStamp
        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!).setValue(hashMap).addOnSuccessListener {
            progressdialog.dismiss()
            Toast.makeText(this,"Account created!",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@RegisterActivity,DashBoardUserActivity::class.java))
            finish()}.addOnFailureListener {
            progressdialog.dismiss()
            Toast.makeText(this,"Failed saving user infor due to ${it.message}",Toast.LENGTH_SHORT).show()
            authFirebase.signOut()
        }
    }


}