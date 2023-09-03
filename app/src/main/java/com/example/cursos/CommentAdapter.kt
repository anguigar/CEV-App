package com.example.cursos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Clase que genera el listado de comentarios

class CommentAdapter(private val commentList: List<String>, private val userList: List<String>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comentario_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        val user = userList[position]
        holder.bind(comment, user)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(comment: String, user: String) {
            val txtComment = itemView.findViewById<TextView>(R.id.txtComment)
            txtComment.text = comment

            val userComment = itemView.findViewById<TextView>(R.id.userComment)
            userComment.text = user
        }
    }
}