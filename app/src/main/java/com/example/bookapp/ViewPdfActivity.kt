package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityViewPdfBinding
import com.example.bookapp.objects.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ViewPdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewPdfBinding
    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackViewPdf.setOnClickListener {
            onBackPressed()
        }

        bookId = intent.getStringExtra("bookId")!!
        loadBookPdf()
    }

    private fun loadBookPdf() {
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.child("url").value
                if (url != null) {
                    loadBookFromUrl(url.toString())
                } else {
                    Log.e("ViewPdfActivity", "URL is null")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewPdfActivity", "Error loading book data: ${error.message}")
            }
        })
    }

    private fun loadBookFromUrl(url: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->
            binding.pdfView.fromBytes(bytes).swipeHorizontal(false)
                .onPageChange { page, pageCount ->
                    val currentPage = page + 1
                    binding.tvSubTitleViewPdf.text = "$currentPage/$pageCount"
                }.onError { error ->
                    Log.e("ViewPdfActivity", "Load Book Error: ${error.message}")
                }.onPageError { page, t ->
                    Log.e("ViewPdfActivity", "Load Page Error: ${t.message}")
                }.load()
            binding.progressViewPdf.visibility = View.GONE
        }.addOnFailureListener { e ->
            Log.e("ViewPdfActivity", "Load Book Error: ${e.message}")
            binding.progressViewPdf.visibility = View.GONE
        }
    }
}
