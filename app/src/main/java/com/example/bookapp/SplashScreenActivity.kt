package com.example.bookapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


private lateinit var firebaseAuth:FirebaseAuth
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed(Runnable {
            checkUser()
        }, 1000)

    }

    private fun checkUser() {
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.uid).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userType = snapshot.child("userType").value
                        if (userType == "admin") {
                            startActivity(
                                Intent(
                                    this@SplashScreenActivity,
                                    DashBoardAdminActivity::class.java
                                )
                            )
                            finish()
                        } else if (userType == "user") {
                            startActivity(
                                Intent(
                                    this@SplashScreenActivity,
                                    DashBoardUserActivity::class.java
                                )
                            )
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}