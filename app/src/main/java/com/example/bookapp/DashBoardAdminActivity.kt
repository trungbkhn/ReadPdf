package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.bookapp.adapter.CategoryAdapter
import com.example.bookapp.databinding.ActivityDashBoardAdminBinding
import com.example.bookapp.model.ModelCategory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

private lateinit var binding: ActivityDashBoardAdminBinding
private lateinit var categoryAdapter: CategoryAdapter
private lateinit var categoryArrayList: ArrayList<ModelCategory>
private lateinit var dialogProgress: ProgressDialog

class DashBoardAdminActivity : AppCompatActivity() {
    val firebaseAuth = FirebaseAuth.getInstance()
    var isSearching = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkInfor()
        categoryArrayList = ArrayList()
        categoryAdapter = CategoryAdapter(this@DashBoardAdminActivity, categoryArrayList)
        loadCategories()
        binding.rcvCategoryAdmin.adapter = categoryAdapter

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this@DashBoardAdminActivity,ProfileActivity::class.java))
        }

        binding.btnLogoutDbAdmin.setOnClickListener {
            MaterialAlertDialogBuilder(this@DashBoardAdminActivity)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out of this app?")
                .setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    firebaseAuth.signOut()
                    startActivity(Intent(this@DashBoardAdminActivity, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        binding.btnAddCategoryAdmin.setOnClickListener {
            startActivity(Intent(this@DashBoardAdminActivity, CategoryAddActivity::class.java))
            finish()
        }
        binding.edtSearchAdmin.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    val userInput = newText.lowercase(Locale.getDefault())
                    var categoryArrayList_filtered = ArrayList<ModelCategory>()
                    for (category in categoryArrayList) {
                        if (category.category.toString().lowercase(Locale.getDefault())
                                .contains(userInput)
                        ) {
                            categoryArrayList_filtered.add(category)
                        }
                    }
                    categoryAdapter.updateCategoryList(categoryArrayList_filtered)
                    isSearching = true
                    //categoryAdapter.updateCategoryList(categoryArrayList)
                } else {
                    isSearching = false
                }
                return true
            }
        })
        binding.btnAddCategoryPdf.setOnClickListener {
            startActivity(Intent(this@DashBoardAdminActivity, UploadPdfActivity::class.java))
        }



    }

    private fun loadCategories() {
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (i in snapshot.children) {
                    val model = i.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                }
                // Cập nhật dữ liệu mới vào adapter
                categoryAdapter.updateCategoryList(categoryArrayList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun checkInfor() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this@DashBoardAdminActivity, MainActivity::class.java))
            finish()
        } else {
            binding.tvGmailDbAdmin.setText(firebaseUser.email)
        }

    }
}