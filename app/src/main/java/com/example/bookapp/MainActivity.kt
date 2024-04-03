package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapp.databinding.ActivityMainBinding

private lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity,LoginActivity::class.java))
        }
    }
}