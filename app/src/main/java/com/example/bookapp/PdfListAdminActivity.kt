package com.example.bookapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.adapter.PdfAdminAdapter
import com.example.bookapp.databinding.ActivityPdfListAdminBinding
import com.example.bookapp.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class PdfListAdminActivity : AppCompatActivity() {
    private var categoryId = ""
    private var categoryTitle = ""
    private lateinit var binding: ActivityPdfListAdminBinding
    private lateinit var pdfList: ArrayList<ModelPdf>
    private lateinit var adapterPdfAdmin: PdfAdminAdapter

    companion object {
        const val TAG = "PDF_LIST"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        categoryTitle = intent.getStringExtra("categoryTitle")!!
        binding.tvPdfCategory.text = categoryTitle
        binding.rcvPdf.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.btnBackPdfAdmin.setOnClickListener {
            onBackPressed()
        }
        adapterPdfAdmin = PdfAdminAdapter(this, ArrayList())
        binding.rcvPdf.adapter = adapterPdfAdmin
        loadPdfList()

        binding.svPdf.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    val userInput = newText.lowercase(Locale.getDefault())
                    var pdfArrayList_filtered = ArrayList<ModelPdf>()
                    for (pdf in pdfList) {
                        if (pdf.title.lowercase(Locale.getDefault())
                                .contains(userInput)
                        ) {
                            pdfArrayList_filtered.add(pdf)
                        }
                    }
                    adapterPdfAdmin.updatePdfList(pdfArrayList_filtered)
                }
                return true
            }
        })
    }


    private fun loadPdfList() {
        pdfList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books").orderByChild("categoryId")
            .equalTo(categoryId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPdf::class.java)
                        pdfList.add(model!!)
                        Log.d(TAG, "OnDataChange: title - ${model.title}; Id -  ${categoryId}")
                    }
                    adapterPdfAdmin.updatePdfList(pdfList) // Cập nhật dữ liệu trong adapter đã khởi tạo từ trước
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    override fun onResume() {
        loadPdfList()
        super.onResume()
    }


}