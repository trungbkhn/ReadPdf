package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.checkerframework.checker.units.qual.A
import java.sql.Timestamp
import java.util.HashMap

private lateinit var binding: ActivityCategoryAddBinding
private lateinit var firebaseAuth: FirebaseAuth
private lateinit var progressdialog: ProgressDialog
class CategoryAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressdialog = ProgressDialog(this)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnSubmit.setOnClickListener {
            addCategory()
        }
        binding.btnBackCategory.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseAuth.currentUser!!.uid).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.child("userType").value
                        if (userType == "admin") {
                            startActivity(
                                Intent(
                                    this@CategoryAddActivity,
                                    DashBoardUserActivity::class.java
                                )
                            )
                            finish()
                        } else if (userType == "user") {
                            startActivity(Intent(this@CategoryAddActivity, DashBoardUserActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }


    private fun addCategory() {
        var categoryName = binding.edtCategory.text.toString().trim()
        var uid = firebaseAuth.uid
        progressdialog.setMessage("Waiting for add your category...")
        //timestamp
        val timeStamp = System.currentTimeMillis()
        //hashmap
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["id"] = timeStamp
        hashMap["category"] = categoryName
        hashMap["timestamp"] = timeStamp
        hashMap["uid"] = uid


        if (categoryName.isEmpty()){
            Toast.makeText(this,"The name of your category must not empty!",Toast.LENGTH_SHORT).show()
        } else {
            val timeStamp = System.currentTimeMillis()
            val ref = FirebaseDatabase.getInstance().getReference("Category")
            ref.child("$timeStamp").setValue(hashMap).addOnSuccessListener {
                progressdialog.dismiss()
                Toast.makeText(this@CategoryAddActivity,"Adding category successed!",Toast.LENGTH_SHORT).show()
                binding.edtCategory.text.clear()
            }.addOnFailureListener {
                progressdialog.dismiss()
                Toast.makeText(this,"Error: ${it.message}",Toast.LENGTH_SHORT).show()
                firebaseAuth.signOut()
            }
        }
    }
}