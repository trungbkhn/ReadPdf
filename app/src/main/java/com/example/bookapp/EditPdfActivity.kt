package com.example.bookapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityEditPdfBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var binding: ActivityEditPdfBinding
private var bookId = ""
private lateinit var progressDialog: ProgressDialog
private lateinit var categoryTitleArrayList: ArrayList<String>
private lateinit var categoryIdArrayList: ArrayList<String>
private const val TAG = "Edit Pdf Activity:"
private var selectedTitleCategory = ""
private var selectedIdCategory = ""
private var Title = ""
private var description = ""

class EditPdfActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBackCategoryUploadEdit.setOnClickListener {
            onBackPressed()
        }
        bookId = intent.getStringExtra("BookId")!!
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.show()
        progressDialog.setCanceledOnTouchOutside(false)
        loadCategoryPdf()
        loadBookInfo()
        binding.tvCategoryUploadEdit.setOnClickListener {
            categoryDiaLog()
        }
        binding.btnUploadEdit.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "Loading book...")
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedIdCategory = snapshot.child("categoryId").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val description = snapshot.child("description").value.toString()
                    binding.edtBookTitleEdit.setText(title)
                    binding.edtBookDescriptionEdit.setText(description)
                    // load category
                    Log.d(TAG, "Load book category...")
                    val refBookCategory =
                        FirebaseDatabase.getInstance().getReference("Category").child(
                            selectedIdCategory
                        )
                    refBookCategory.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val category = snapshot.child("category").value.toString()
                            binding.tvCategoryUploadEdit.text = category
                            Log.d(TAG, "Load book category success!")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Load book category failed to ${error.message}")
                            Toast.makeText(
                                this@EditPdfActivity, "Error: ${error.message}", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun validateData() {
        Title = binding.edtBookTitleEdit.text.toString().trim()
        description = binding.edtBookDescriptionEdit.text.toString().trim()
        if (Title.isNullOrEmpty()) {
            Toast.makeText(this, "The title of book must not empty!", Toast.LENGTH_SHORT).show()
        } else if (description.isNullOrEmpty()) {
            Toast.makeText(this, "Description of the book must not empty!", Toast.LENGTH_SHORT)
                .show()
        } else {
            updatePdf()
        }

    }

    private fun updatePdf() {
        Log.d(TAG, "Update Pdf")
        progressDialog.setMessage("Updating...")
        progressDialog.show()
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["title"] = Title
        hashmap["description"] = description
        hashmap["categoryId"] = selectedIdCategory
        ref.updateChildren(hashmap).addOnSuccessListener {
            progressDialog.dismiss()
            Log.d(TAG, "Update Pdf Success!")
            Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error update: ${it.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error update due to :${it.message}")
            progressDialog.dismiss()
        }
    }

    private fun categoryDiaLog() {
        val categoryArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoryArray[i] = categoryTitleArrayList[i]
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category").setItems(categoryArray) { dialog, pos ->
            selectedIdCategory = categoryIdArrayList[pos]
            selectedTitleCategory = categoryTitleArrayList[pos]
            binding.tvCategoryUploadEdit.text = selectedTitleCategory
        }.show()
    }

    private fun loadCategoryPdf() {
        Log.d(TAG, "Loading categories")
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()
                for (i in snapshot.children) {
                    val id = i.child("id").value
                    val title = i.child("category").value
                    categoryIdArrayList.add(id.toString())
                    categoryTitleArrayList.add(title.toString())
                    Log.d(TAG, "onDataChange Category ID: ${id}")
                    Log.d(TAG, "onDataChange Category Title: ${title}")
                    progressDialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error due to ${error.message}")
                Toast.makeText(this@EditPdfActivity, "Error:${error.message}", Toast.LENGTH_SHORT)
                    .show()
                progressDialog.dismiss()
            }
        })

    }
}