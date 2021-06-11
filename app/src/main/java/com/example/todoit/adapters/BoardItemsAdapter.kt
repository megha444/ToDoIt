package com.example.todoit.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todoit.R
import com.example.todoit.model.Board

open class BoardItemsAdapter(private val context: Context,
                             private val list: ArrayList<Board>):
RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board,
                    parent,
                    false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model= list[position]

        if(holder is MyViewHolder){
            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_board_image_main_rv))

            holder.itemView.findViewById<TextView>(R.id.tv_name_rv_main).text= model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by_rv).text= "Created By: ${model.createdBy}"

            Log.i("Attaching Board number", "" +model.name)
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun getItemCount(): Int {
        return  list.size
    }

private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){

}

}