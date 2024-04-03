package com.example.bookapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.adapter.PdfUserAdapter
import com.example.bookapp.databinding.FragmentBookUserBinding
import com.example.bookapp.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookUserFragment : Fragment {
    private lateinit var binding: FragmentBookUserBinding
    private lateinit var pdfUserAdapter: PdfUserAdapter
    private lateinit var pdfUserList: ArrayList<ModelPdf>
    private var category = ""
    private var categoryId = ""
    private var uid = ""

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = arguments
        if (arg != null) {
            category = arg.getString("category")!!
            categoryId = arg.getString("categoryId")!!
            uid = arg.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookUserBinding.inflate(inflater, container, false)
        binding.rcvPdfUser.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        if (category == "All") {
            loadAllBooks()
        } else if (category == "Most Viewed") {
            loadMostViewedBooks("viewsCount")
        } else if (category == "Most Downloaded") {
            loadMostDownloadedBooks("downloadsCount")
        } else {
            loadCategorizedBooks()
        }

        binding.svSearchUser.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    val userInput = newText.lowercase()
                    val bookArrayListFiltered = ArrayList<ModelPdf>()
                    for (book in pdfUserList) {
                        if (book.title.lowercase().contains(userInput)) {
                            bookArrayListFiltered.add(book)
                        }
                    }
                    if (::pdfUserAdapter.isInitialized) {
                        pdfUserAdapter.updatePdfListUser(bookArrayListFiltered)
                    }
                }
                return true
            }
        })

        return binding.root
    }

    private fun loadMostDownloadedBooks(orderBy: String) {
        pdfUserList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToFirst(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val model = i.getValue(ModelPdf::class.java)
                        model?.let { pdfUserList.add(it) }
                    }
                    pdfUserAdapter = PdfUserAdapter(requireContext(), pdfUserList)
                    binding.rcvPdfUser.adapter = pdfUserAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "loadMostDownloadedBooks : Failed due to ${error.message}")
                }
            })
    }

    private fun loadMostViewedBooks(orderBy: String) {
        pdfUserList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToFirst(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val model = i.getValue(ModelPdf::class.java)
                        model?.let { pdfUserList.add(it) }
                    }
                    pdfUserAdapter = PdfUserAdapter(requireContext(), pdfUserList)
                    binding.rcvPdfUser.adapter = pdfUserAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "MostViewedBook : Failed due to ${error.message}")
                }
            })
    }

    private fun loadAllBooks() {
        pdfUserList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfUserList.clear()
                for (i in snapshot.children) {
                    val model = i.getValue(ModelPdf::class.java)
                    model?.let { pdfUserList.add(it) }
                }
                pdfUserAdapter = PdfUserAdapter(requireContext(), pdfUserList)
                binding.rcvPdfUser.adapter = pdfUserAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadAllBooks : Failed due to ${error.message}")
            }
        })
    }

    private fun loadCategorizedBooks() {
        pdfUserList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val model = i.getValue(ModelPdf::class.java)
                        model?.let { pdfUserList.add(it) }
                    }
                    pdfUserAdapter = PdfUserAdapter(requireContext(), pdfUserList)
                    binding.rcvPdfUser.adapter = pdfUserAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "MostViewedBook : Failed due to ${error.message}")
                }
            })
    }

    companion object {
        const val TAG = "BOOK_USER_TAG"

        fun newInstance(categoryId: String, category: String, uid: String): BookUserFragment {
            val fragment = BookUserFragment()
            val argument = Bundle()
            argument.putString("categoryId", categoryId)
            argument.putString("category", category)
            argument.putString("uid", uid)
            fragment.arguments = argument
            return fragment
        }
    }
}
