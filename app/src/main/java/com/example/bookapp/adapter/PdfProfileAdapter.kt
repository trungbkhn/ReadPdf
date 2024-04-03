package com.example.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bookapp.PdfDetailsActivity
import com.example.bookapp.application.MyApplication
import com.example.bookapp.databinding.LayoutPdfFavBinding
import com.example.bookapp.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PdfProfileAdapter : RecyclerView.Adapter<PdfProfileAdapter.HolderPdfProfile> {
    private val context: Context
    private var bookList: ArrayList<ModelPdf>
    private lateinit var binding: LayoutPdfFavBinding

    constructor(context: Context, bookList: ArrayList<ModelPdf>) {
        this.context = context
        this.bookList = bookList
    }


    inner class HolderPdfProfile(itemView: View) : ViewHolder(itemView) {
        val pdfProgressBar = binding.pgbProgressBar
        val pdfBookTitle = binding.tvBookTitle
        val pdfSize = binding.tvSizePdf
        val pdfDate = binding.tvDatePdf
        val pdfCategory = binding.tvCategoryPdf
        val pdfDescription = binding.tvDescriptionPdf
        val pdfView = binding.pdfView
        val btn_fav = binding.btnFav
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfProfile {
        binding = LayoutPdfFavBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfProfile(binding.root)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: HolderPdfProfile, position: Int) {
        val model = bookList[position]
        loadBookFav(model, holder)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
        holder.btn_fav.setOnClickListener {
            MyApplication.removeFavouriteBook(model.id, context)
        }

    }

    private fun loadBookFav(model: ModelPdf, holder: PdfProfileAdapter.HolderPdfProfile) {
        val bookId = model.id
        //get data from db
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = "${snapshot.child("categoryId").value}"
                val description = "${snapshot.child("description").value}"
                val timeStamp = "${snapshot.child("timestamp").value}"
                val title = "${snapshot.child("title").value}"
                val uid = "${snapshot.child("uid").value}"
                val url = "${snapshot.child("url").value}"
                val downloadsCount = "${snapshot.child("downloadsCount").value}"
                val viewsCount = "${snapshot.child("viewsCount").value}"
                val date = MyApplication.formatTimestamp(timeStamp.toLong())
                Log.d("AdapterProFile", " -timeStamp: $timeStamp \n -title: $title")
                holder.pdfBookTitle.text = title
                holder.pdfDescription.text = description
                holder.pdfDate.text = date

                model.isFav = true
                model.downloadsCount = downloadsCount.toLong()
                model.title = title
                model.viewsCount = viewsCount.toLong()
                model.timestamp = timeStamp.toLong()
                model.url = url
                model.uid = uid
                model.categoryId = categoryId
                model.description = description

                MyApplication.loadPdfFromSinglePage(
                    url,
                    title,
                    holder.pdfView,
                    holder.pdfProgressBar,
                    null
                )
                MyApplication.loadPdfSize(url, title, holder.pdfSize)

                MyApplication.loadCategory(categoryId, holder.pdfCategory)




            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}