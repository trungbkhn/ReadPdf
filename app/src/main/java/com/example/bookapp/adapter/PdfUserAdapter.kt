package com.example.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bookapp.PdfDetailsActivity
import com.example.bookapp.application.MyApplication
import com.example.bookapp.databinding.LayoutRowPdfUserBinding
import com.example.bookapp.model.ModelPdf

class PdfUserAdapter: RecyclerView.Adapter<PdfUserAdapter.HolderPdfUser> {
    private val context: Context
    private lateinit var binding: LayoutRowPdfUserBinding
    var pdfUserArrayList: ArrayList<ModelPdf>

    constructor(
        context: Context,
        pdfUserArrayList: ArrayList<ModelPdf>,
    ) : super() {
        this.context = context
        this.pdfUserArrayList = pdfUserArrayList

    }


    inner class HolderPdfUser(itemView: View): ViewHolder(itemView){
        val pdfProgressBar = binding.pgbProgressBarUser
        val pdfBookTitle = binding.tvBookTitleUser
        val pdfSize = binding.tvSizePdfUser
        val pdfDate = binding.tvDatePdfUser
        val pdfCategory = binding.tvCategoryPdfUser
        val pdfDescription = binding.tvDescriptionPdfUser
        val pdfView = binding.pdfViewUser
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = LayoutRowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfUserArrayList[position]
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
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId",id)
            context.startActivity(intent)
//            (context as Activity).finish()
        }
    }

    override fun getItemCount(): Int {
        return pdfUserArrayList.size
    }

    fun updatePdfListUser(pdfList: ArrayList<ModelPdf>) {
        pdfUserArrayList.clear()
        pdfUserArrayList = ArrayList()
        pdfUserArrayList.addAll(pdfList)
        notifyDataSetChanged()
    }
}