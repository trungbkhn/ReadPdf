package com.example.bookapp.adapter

import android.widget.Filter
import com.example.bookapp.model.ModelCategory

class FilterCategory: Filter {
    private val filterArrayList : ArrayList<ModelCategory>
    private val filterAdapter: CategoryAdapter

    constructor(filterArrayList: ArrayList<ModelCategory>, filterAdapter: CategoryAdapter) {
        this.filterArrayList = filterArrayList
        this.filterAdapter = filterAdapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val constraint = constraint.toString().uppercase()
        val result = FilterResults()
        if (constraint != null && constraint.isNotEmpty()){
            val filteredModel :ArrayList<ModelCategory> = ArrayList()
            for(i in 0 until filterArrayList.size){
                if (filterArrayList[i].category.uppercase().contains(constraint)){
                    filteredModel.add(filterArrayList[i])
                }
            }
            result.count = filteredModel.size
            result.values = filteredModel

        }else{
            result.count = filterArrayList.size
            result.values = filterArrayList
        }
        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changed
        filterAdapter.categoryArrayList = results.values as ArrayList<ModelCategory>
        //notify change
        filterAdapter.notifyDataSetChanged()
    }
}