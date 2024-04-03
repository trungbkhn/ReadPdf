package com.example.bookapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.bookapp.R
import com.example.bookapp.application.MyApplication
import com.example.bookapp.databinding.LayoutCustomCommentBinding
import com.example.bookapp.model.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.HolderComment> {
    private lateinit var binding: LayoutCustomCommentBinding
    private val context: Context
    private val commentArrayList: ArrayList<ModelComment>
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderComment(itemView: View) : ViewHolder(itemView) {
        val comment = binding.tvComment
        val name = binding.tvName
        val date = binding.tvDate
        val image = binding.imgVProfileComment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = LayoutCustomCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = commentArrayList[position]
        val id = model.id
        val bookId = model.bookId
        val timestamp = model.timestamp
        val comment = model.comment
        val uid = model.uid
        holder.comment.text = comment
        val date = MyApplication.formatTimestamp(timestamp.toLong())
        holder.date.text = date
        loadUserDetails(model, holder) //name, image
        holder.itemView.setOnLongClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }

            true
        }
    }

    private fun deleteCommentDialog(model: ModelComment, holder: CommentAdapter.HolderComment) {
        val bookId = model.bookId
        val id = model.id
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Detele Comment")
            .setMessage("Are you sure you want to delete your comment?")
            .setPositiveButton("Yes") { d, e ->
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                    .child(bookId).child("Comments").child(id)
                ref.removeValue().addOnSuccessListener { Toast.makeText(context,"Deleted!",Toast.LENGTH_SHORT).show() }.addOnFailureListener {
                    Toast.makeText(context,"Failed delete comment: ${it.message}",Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("No"){d,e ->
                d.dismiss()
            }.show()
    }

    private fun loadUserDetails(model: ModelComment, holder: CommentAdapter.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = "${snapshot.child("name").value}"
                val profileImage = "${snapshot.child("profileImage").value}"
                holder.name.text = name
                Log.d("loadComment: ","loadUserDetails name = $name")
                try {
                    Glide.with(context).load(profileImage).placeholder(R.drawable.ic_person_gray)
                        .into(holder.image)
                } catch (e: Exception) {
                    holder.image.setImageResource(R.drawable.ic_person_gray)
                    Log.e("Load Comment--", "Failed load UserDetails due to ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


}