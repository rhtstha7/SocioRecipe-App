package com.myproject.sociorecipe

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.saved_post_layout.view.*

class SavedPostAdapter: BaseAdapter {
    var postList=ArrayList<Post>()
    var context:Context
    constructor(postList: ArrayList<Post>, context: Context){// getting array list and context to set adapter
        this.postList=postList
        this.context=context
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //separating data
        val recipeName=postList[position].recipeName
        val recipeDetails=postList[position].recipeDetails
        val recipeImage=postList[position].recipeImageByteArray
        //inflating custom view
        val view=LayoutInflater.from(context).inflate(R.layout.saved_post_layout,null)
        //changing values
        view.saved_post_name.setText(recipeName)
        Glide.with(context).load(recipeImage).into(view.saved_post_image)
        //on click of post from saved post
        view.setOnClickListener{
            //send user to another activity of opened post with data in bundle
            val intent=Intent((context as Activity), SavedPostActivity::class.java)
            val bundel=Bundle()
            bundel.putByteArray("recipeImage",recipeImage)
            bundel.putString("recipeName",recipeName)
            bundel.putString("recipeDetails",recipeDetails)
            intent.putExtras(bundel)
            context.startActivity(intent)
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return postList.size
    }
}