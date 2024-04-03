package com.example.bookapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bookapp.CategoryAddActivity
import com.example.bookapp.PdfListAdminActivity
import com.example.bookapp.databinding.LayoutCategoryBinding
import com.example.bookapp.model.ModelCategory
import com.google.firebase.database.FirebaseDatabase
import org.checkerframework.checker.units.qual.C

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.HoldCategory>, Filterable {

    private val context: Context
    var categoryArrayList: ArrayList<ModelCategory>
    private var filterArrayList: ArrayList<ModelCategory>
    private var filter: FilterCategory? =null
    private lateinit var binding: LayoutCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterArrayList = categoryArrayList
    }

inner class HoldCategory(private val binding: LayoutCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
    var categoryName = binding.tvCategory
    var btn_delete = binding.btnDelete
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoldCategory {
        val binding = LayoutCategoryBinding.inflate(LayoutInflater.from(context),parent,false)
        return HoldCategory(binding)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun onBindViewHolder(holder: HoldCategory, position: Int) {
        //get data
        var model = categoryArrayList[position]
        var id = model.id
        var uid = model.uid
        var timestamp = model.timestamp
        var category = model.category
        //set data

        holder.categoryName.text = category

        //handle click
        holder.btn_delete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
            builder.setMessage("Are sure you want to delete this category?")
                .setPositiveButton("Confirm") { a, d ->
                    deleteCategory(model, holder)

                }.setNegativeButton("Cancel") { a, d ->
                a.dismiss()
            }.show()
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id.toString())
            intent.putExtra("categoryTitle", model.category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HoldCategory) {
        val idCat = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Category")
            .child(idCat.toString()).removeValue().addOnSuccessListener {
                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Error delete: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterCategory(filterArrayList,this)
        }
        return filter as FilterCategory
    }
    fun updateCategoryList(categoryList: ArrayList<ModelCategory>) {
        categoryArrayList = ArrayList()
        categoryArrayList.addAll(categoryList)
        notifyDataSetChanged()
    }
}