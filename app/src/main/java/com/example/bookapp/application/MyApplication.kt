package com.example.bookapp.application

import android.annotation.SuppressLint
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookapp.PdfDetailsActivity
import com.example.bookapp.ProfileActivity
import com.example.bookapp.objects.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return android.text.format.DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, pdfFile: String, tv_size: TextView) {
            val TAG = "PDF_SIZE_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener { metadata ->
                Log.d(TAG, "Load PDF size: got meta data.")
                val bytes = metadata.sizeBytes.toDouble()
                Log.d(TAG, "Load PDF size: Size Bytes $bytes")
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb >= 1) {
                    tv_size.text = "${String.format("%.2f", mb)}MB"
                } else if (kb >= 1) {
                    tv_size.text = "${String.format("%.2f", kb)}KB"
                } else {
                    tv_size.text = "${String.format("%.2f", bytes)}B"
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error: ${exception.message}")
            }
        }

        fun loadPdfFromSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            tv_pages: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "LoadPdfSize: Size Bytes ${bytes}.")
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.e(TAG, "LoadPdfFromSinglePage: ${t.message}")
                        }.onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.e(
                                TAG,
                                "LoadPdfFromSinglePage: Error loading page $page: ${t.message}"
                            )
                        }.onLoad { nbPages ->
                            progressBar.visibility = View.INVISIBLE
                            if (tv_pages != null) {
                                tv_pages.text = "$nbPages"
                            }
                        }.load()
                }.addOnFailureListener { exception ->
                    progressBar.visibility = View.INVISIBLE
                    Log.e(TAG, "Fail due to ${exception.message}")
                }
        }

        fun loadCategory(categoryId: String, tv_category: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Category").child(categoryId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"
                        tv_category.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "-deleting ${bookTitle} book:")
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting ${bookTitle} book ...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete().addOnSuccessListener {
                Log.d(TAG, "Delete book from storage successfully!")
                progressDialog.dismiss()
                val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
                ref.removeValue().addOnSuccessListener {
                    Log.d(TAG, "Delete book from database successfully!")
                    Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Log.e(TAG, "Error delete from database: ${it.message}")
                    Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
                .addOnFailureListener {
                    Log.e(TAG, "Error delete from storage: ${it.message}")
                    progressDialog.dismiss()
                    Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            val TAG = "IncrementBookViewCount-"
            val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var viewCount =
                        snapshot.child("viewsCount").value?.toString() // Lấy giá trị viewCount và chuyển đổi sang chuỗi

                    // Kiểm tra nếu viewCount là null hoặc rỗng
                    if (viewCount.isNullOrEmpty()) {
                        viewCount = "0" // Gán giá trị mặc định là 0
                    }

                    val newCount = viewCount.toLong() + 1
                    val hashMap = HashMap<String, Any>()
                    hashMap["viewsCount"] = newCount
                    val refDb = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
                    refDb.updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "Succeed!") // Ghi log khi cập nhật thành công
                        }
                        .addOnFailureListener { error ->
                            Log.e(
                                TAG,
                                "Error: ${error.message}"
                            ) // Ghi log nếu có lỗi xảy ra khi cập nhật
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error: ${error.message}")
                }
            })
        }

        @SuppressLint("SuspiciousIndentation")
        fun removeFavouriteBook(bookId: String, context: Context) {
            val fireBaseAuth = FirebaseAuth.getInstance()
            Log.d(ProfileActivity.TAG, "removingFavouriteBook: is removing...")
            val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(fireBaseAuth.currentUser!!.uid).child("Favourites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(PdfDetailsActivity.TAG, "Remove book favourite succeed!, BookId: $bookId")
                    Toast.makeText(context, "Removed book to your favourite", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                    Log.e(PdfDetailsActivity.TAG, "Fail update Remove book due to ${it.message}")
                    Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }


}