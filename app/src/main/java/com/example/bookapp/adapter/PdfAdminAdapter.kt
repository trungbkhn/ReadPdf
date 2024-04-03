package com.example.bookapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.EditPdfActivity
import com.example.bookapp.PdfDetailsActivity
import com.example.bookapp.application.MyApplication
import com.example.bookapp.databinding.LayoutPdfBinding
import com.example.bookapp.model.ModelPdf
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PdfAdminAdapter : RecyclerView.Adapter<PdfAdminAdapter.HolderPdfAdmin>, Filterable {


    private val context: Context
    var pdfArrayList: ArrayList<ModelPdf>
    private var filterList: ArrayList<ModelPdf>
    private lateinit var binding: LayoutPdfBinding
    var filter: FilterPdfAdmin? = null

    constructor(
        context: Context,
        pdfArrayList: ArrayList<ModelPdf>,
    ) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfProgressBar = binding.pgbProgressBar
        val pdfBookTitle = binding.tvBookTitle
        val pdfSize = binding.tvSizePdf
        val pdfDate = binding.tvDatePdf
        val pdfCategory = binding.tvCategoryPdf
        val pdfDescription = binding.tvDescriptionPdf
        val pdfView = binding.pdfView
        val btn_more = binding.btnMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = LayoutPdfBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfTitle = model.title
        val id = model.id
        val pdfCategoryId = model.categoryId
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val pdfDescription = model.description
        val pdfDate = MyApplication.formatTimestamp(timestamp)
        holder.pdfBookTitle.text = pdfTitle
        holder.pdfDescription.text = pdfDescription
        holder.pdfDate.text = pdfDate
        MyApplication.loadCategory(pdfCategoryId, holder.pdfCategory)
        MyApplication.loadPdfFromSinglePage(
            pdfUrl,
            pdfTitle,
            holder.pdfView,
            holder.pdfProgressBar,
            null
        )
        MyApplication.loadPdfSize(pdfUrl, pdfTitle, holder.pdfSize)
        holder.btn_more.setOnClickListener {
            moreOptionsDialog(model, holder)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", id)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: PdfAdminAdapter.HolderPdfAdmin) {
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title
        val options = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option").setItems(options) { dialog, pos ->
            if (pos == 0) {
                val intent = Intent(context, EditPdfActivity::class.java)
                intent.putExtra("BookId", bookId)
                context.startActivity(intent)
            } else if (pos == 1) {
                //delete
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.dismiss()
                        MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()

                    }
                    .show()
            }
        }.show()
    }


    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, adapterPdfAdmin = this)
        }
        return filter as FilterPdfAdmin
    }

    fun updatePdfList(pdfList: ArrayList<ModelPdf>) {
        pdfArrayList = ArrayList()
        pdfArrayList.addAll(pdfList)
        notifyDataSetChanged()
    }


}