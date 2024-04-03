package com.example.bookapp

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.adapter.CommentAdapter
import com.example.bookapp.application.MyApplication
import com.example.bookapp.databinding.ActivityPdfDetailsBinding
import com.example.bookapp.databinding.CustomDialogCommentBinding
import com.example.bookapp.model.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfDetailsBinding
    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""
    private lateinit var progressDialog: ProgressDialog
    private var downloadedBytes: ByteArray? = null
    private var isFavouriteBook = false
    private var fireBaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var comment = ""
    private lateinit var commentList: ArrayList<ModelComment>
    private lateinit var commentAdapter: CommentAdapter

    companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "STORAGE PERMISSION is already granted")
                downloadBook()
            } else {
                Log.e(TAG, "STORAGE PERMISSION is denied")
            }
        }

    private val saveDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            uri?.let { saveToDocument(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (fireBaseAuth.currentUser != null) {
            checkIsFavourite()
        }
        progressDialog = ProgressDialog(this)
        bookId = intent.getStringExtra("bookId") ?: ""
        binding.btnBackPdfDetails.setOnClickListener {
            onBackPressed()
            finish()
        }

        binding.btnReadPdf.setOnClickListener {
            val intent = Intent(this@PdfDetailsActivity, ViewPdfActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.btnDownloadPdf.setOnClickListener {
            checkPermissionAndDownload()
        }

        binding.btnFavouritePdf.setOnClickListener {
            if (fireBaseAuth.currentUser == null) {
                Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT).show()
            } else {
                if (isFavouriteBook) {
                    removeFavouriteBook()
                } else {
                    addFavouriteBook()
                }
            }
        }
        binding.btnAddComment.setOnClickListener {
            if (fireBaseAuth.currentUser == null) {
                Toast.makeText(this, "You are not log in!", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }

        MyApplication.incrementBookViewCount(bookId)
        loadPdfDetails()
        loadComment()
    }

    private fun loadComment() {
        commentList = ArrayList()
        val ref =
            FirebaseDatabase.getInstance().getReference("Books").child(bookId).child("Comments")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (i in snapshot.children) {
                    val model = i.getValue(ModelComment::class.java)
                    commentList.add(model!!)
                    Log.d(TAG, "loadComment: commentList size: ${commentList.size}")
                }
                commentAdapter = CommentAdapter(this@PdfDetailsActivity, commentList)
                binding.rcvComment.adapter = commentAdapter
                binding.rcvComment.layoutManager = LinearLayoutManager(this@PdfDetailsActivity)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun addCommentDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        val commentAddBinding = CustomDialogCommentBinding.inflate(LayoutInflater.from(this))
        builder.setView(commentAddBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.btnBackDialogComment.setOnClickListener {
            alertDialog.dismiss()
        }
        commentAddBinding.btnSummit.setOnClickListener {
            comment = commentAddBinding.edtComment.text.toString().trim()
            if (comment.isNullOrEmpty()) {
                Toast.makeText(this, "Enter your comment...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        progressDialog.setMessage("Adding Comment")
        progressDialog.show()
        val timeStamp = "${System.currentTimeMillis()}"
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["id"] = timeStamp
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timeStamp
        hashMap["comment"] = comment
        hashMap["uid"] = "${fireBaseAuth.uid}"

        // books> bookId> Comments> commentId> commentData
        val ref =
            FirebaseDatabase.getInstance().getReference("Books").child(bookId).child("Comments")
                .child(timeStamp).setValue(hashMap).addOnSuccessListener {
                    progressDialog.dismiss()
                    commentAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Can not comment, ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }

    }

    private fun checkIsFavourite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(fireBaseAuth.currentUser!!.uid)
            .child("Favourites")  // Thêm "Favourites" vào tham chiếu ở đây
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "checkFavouriteBook: Succeed!")
                isFavouriteBook = snapshot.child(bookId).exists()
                if (isFavouriteBook) {
                    Log.d(TAG, "Da like, BookId: $bookId")
                } else {
                    Log.d(TAG, "Chua like, BookId: $bookId")
                }
                updateFavouriteButtonUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error checking Favourite Book: ${error.message}")
            }
        })
    }


    private fun updateFavouriteButtonUI() {
        if (isFavouriteBook) {
            binding.btnFavouritePdf.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                R.drawable.ic_favorite,
                0,
                0
            )
            binding.btnFavouritePdf.text = "Unlike"

        } else {
            binding.btnFavouritePdf.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                R.drawable.ic_unfavorite,
                0,
                0
            )
            binding.btnFavouritePdf.text = "Like"
        }
    }

    private fun addFavouriteBook() {
        Log.d(TAG, "checkFavourite: is checking...")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(fireBaseAuth.currentUser!!.uid).child("Favourites").child(bookId)
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["bookId"] = bookId
        ref.setValue(hashMap).addOnSuccessListener {
            Log.d(TAG, "Add book favourite succeed!")
            Toast.makeText(this, "Added book to your favourite", Toast.LENGTH_SHORT).show()
            isFavouriteBook = true
            updateFavouriteButtonUI()
        }.addOnFailureListener {
            Log.e(TAG, "Fail update favourite book due to ${it.message}")
            Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFavouriteBook() {
        Log.d(TAG, "removingFavouriteBook: is removing...")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(fireBaseAuth.currentUser!!.uid).child("Favourites").child(bookId).removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Remove book favourite succeed!, BookId: $bookId")
                Toast.makeText(this, "Removed book to your favourite", Toast.LENGTH_SHORT).show()
                isFavouriteBook = false
                updateFavouriteButtonUI()
            }.addOnFailureListener {
                Log.e(TAG, "Fail update Remove book due to ${it.message}")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkPermissionAndDownload() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            downloadBook()
            Log.d(TAG, "STORAGE PERMISSION is already granted")
        } else {
            Log.e(TAG, "STORAGE PERMISSION was not granted")
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun downloadBook() {
        progressDialog.setMessage("Downloading...")
        progressDialog.show()
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageRef.getBytes(com.example.bookapp.objects.Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "Download book: succeed!")
                downloadedBytes = bytes
                saveWithSAF()
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Download book: fail due to ${exception.message}")
                Toast.makeText(
                    this@PdfDetailsActivity,
                    "Failed to save due to ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveWithSAF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "$bookId.pdf")
        }
        saveDocumentLauncher.launch("application/pdf")
    }

    private fun saveToDocument(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { outputStream ->
                    downloadedBytes?.let { bytes ->
                        outputStream.write(bytes)
                    }
                }
            }
            Toast.makeText(this@PdfDetailsActivity, "Saved!", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            incrementDownloadCount()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.e(TAG, "Error saving download: ${e.message}")
            Toast.makeText(
                this@PdfDetailsActivity,
                "Failed to save due to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun incrementDownloadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var downloadsCount = "${snapshot.child("downloadsCount").value}"
                if (downloadsCount == "" || downloadsCount == "null") {
                    downloadsCount = "0"
                }
                val newDownloadsCount = downloadsCount.toLong() + 1
                val hashMap: HashMap<String, Any> = HashMap()
                hashMap["downloadsCount"] = newDownloadsCount
                val dbRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
                dbRef.updateChildren(hashMap).addOnSuccessListener {
                    Log.d(TAG, "onDataChange: Succeed update DownloadsCount!")
                }.addOnFailureListener {
                    Log.e(TAG, "onDataChange: Failed update DownloadsCount due to ${it.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: Failed update DownloadsCount due to ${error.message}")
            }
        })
    }

    private fun loadPdfDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = snapshot.child("categoryId").value.toString()
                val description = snapshot.child("description").value.toString()
                val downloadsCount = snapshot.child("downloadsCount").value.toString()
                val viewsCount = snapshot.child("viewsCount").value.toString()
                bookTitle = snapshot.child("title").value.toString()
                val uid = snapshot.child("uid").value.toString()
                val timestamp = "${snapshot.child("timestamp").value}"
                bookUrl = snapshot.child("url").value.toString()
                val date = MyApplication.formatTimestamp(timestamp.toLong())
                MyApplication.loadPdfFromSinglePage(
                    bookUrl, bookTitle, binding.pdfViewDetails,
                    binding.progressPdfView, binding.tvPagesDetails
                )
                MyApplication.loadPdfSize(bookUrl, bookTitle, binding.tvSizeDetail)
                MyApplication.loadCategory(categoryId, binding.tvCategoryDetail)
                binding.tvBookTitle.text = bookTitle
                binding.tvDateDetail.text = date
                binding.tvDownloadsDetails.text = downloadsCount
                binding.tvViewsDetail.text = viewsCount
                binding.tvDescriptionPdfDetails.text = description
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: Failed to load PDF details due to ${error.message}")
            }
        })
    }
}

