package com.example.bookapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityUploadPdfBinding
import com.example.bookapp.model.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

private lateinit var binding: ActivityUploadPdfBinding
private lateinit var firebaseAuth: FirebaseAuth
private lateinit var progressDialog: ProgressDialog
private lateinit var categoryArraylist: ArrayList<ModelCategory>
private var TAG = "PDF_ADD_TAG"
private var uri: Uri? = null
private var selectedCategoryId = ""
private var selectedCategoryTitle = ""
private var Title = ""
private var description = ""
private var category = ""


class UploadPdfActivity : AppCompatActivity() {
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                uri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        TAG = ""
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        loadPDFCategory()
        binding.tvCategoryUpload.setOnClickListener {
            categoryPickDialog()
        }
        binding.btnPdfCategoryUpload.setOnClickListener {
            pdfPickIntent()
        }
        binding.btnBackCategoryUpload.setOnClickListener {
            super.onBackPressed()
        }
        binding.btnUpload.setOnClickListener {
            // validate data
            validateData()
        }


    }

    private fun validateData() {
        Log.d(TAG, "Validating Data:")
        Title = binding.edtBookTitle.text.toString().trim()
        description = binding.edtBookDescription.text.toString().trim()
        category = binding.tvCategoryUpload.text.toString().trim()
        if (Title.isNullOrEmpty()) {
            Toast.makeText(this, "Title must not null!", Toast.LENGTH_SHORT).show()
        } else if (description.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter your description!", Toast.LENGTH_SHORT).show()
        } else if (category.isNullOrEmpty()) {
            Toast.makeText(this, "Please pick a category!", Toast.LENGTH_SHORT).show()
        } else if (uri == null) {
            Toast.makeText(this, "Please pick Pdf...", Toast.LENGTH_SHORT).show()
        } else {
            upLoadCategoryPdfToStorage()
        }

    }

    private fun upLoadCategoryPdfToStorage() {
        Log.d(TAG, "UploadPdfToStorage...")
        progressDialog.setTitle("Uploading Pdf1...")
        progressDialog.show()
        //timestamp
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Books/$timestamp"
        val storageRef = FirebaseStorage.getInstance().getReference("Books").child("$timestamp")
        storageRef.putFile(uri!!).addOnSuccessListener {taskSnapshot ->
            Log.d(TAG, "UploadPdfToStorage: getting Url...")
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadPdfUrl = "${uriTask.result}"
            uploadPdfIntoDb(uploadPdfUrl, timestamp)
        }.addOnFailureListener {
            Log.e(TAG, "UploadPdfToStorage: Failing to upload Pdf due to: ${it.message}")
            progressDialog.dismiss()
            Toast.makeText(this, "Failing to upload Pdf due to: ${it.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun uploadPdfIntoDb(uploadPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "UploadPdfToStorage: uploading into DB")
        progressDialog.setMessage("Uploading Pdf2...")
        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$Title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            Log.d(TAG, "UploadPdfToStorage: Successed!")
            progressDialog.dismiss()
            uri = null
            Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Log.e(
                TAG,
                "UploadPdfToStorage2: Fail to upload Pdf into DB due to ${it.message}"
            )
            progressDialog.dismiss()
            Toast.makeText(this, "Failing to upload Pdf due to: ${it.message}", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun loadPDFCategory() {
        Log.d(TAG, "Load PDF category")
        categoryArraylist = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArraylist.clear()
                for (i in snapshot.children) {
                    val model = i.getValue(ModelCategory::class.java)
                    categoryArraylist.add(model!!)
                    Log.d(TAG, " onDataChanged:  ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun categoryPickDialog() {
        Log.d(TAG, " categoryPickDiaLog")
        val categories_Name_Array = arrayOfNulls<String>(categoryArraylist.size)
        for (i in categoryArraylist.indices) {
            categories_Name_Array[i] = categoryArraylist[i].category
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category").setItems(categories_Name_Array) { dialog, which ->
            selectedCategoryId = categoryArraylist[which].id.toString()
            selectedCategoryTitle = categoryArraylist[which].category
            binding.tvCategoryUpload.text = selectedCategoryTitle
            Log.d(TAG, "CATEGORY PICK DIALOG: Selected Category ID: $selectedCategoryId")
            Log.d(TAG, "CATEGORY PICK DIALOG: Selected Category Title: $selectedCategoryTitle")
        }.show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }


}