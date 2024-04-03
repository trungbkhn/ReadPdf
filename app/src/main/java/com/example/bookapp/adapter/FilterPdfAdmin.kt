package com.example.bookapp.adapter

import android.widget.Filter
import com.example.bookapp.model.ModelPdf
import com.google.firebase.database.core.Constants

class FilterPdfAdmin : Filter {
    var filterList: ArrayList<ModelPdf>
    var adapterPdfAdmin: PdfAdminAdapter

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: PdfAdminAdapter) : super() {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().toLowerCase()
            val filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                if (filterList[i].title.lowercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }
            results.values = filteredModels
            results.count = filteredModels.size

        } else {
            results.values = filterList
            results.count = filterList.size
        }
        return results

    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>
        adapterPdfAdmin.notifyDataSetChanged()
    }

    override fun convertResultToString(resultValue: Any?): CharSequence {

        return super.convertResultToString(resultValue)
    }
}