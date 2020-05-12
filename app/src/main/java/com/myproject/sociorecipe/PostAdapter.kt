package com.myproject.sociorecipe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.post_layout.view.*

class PostAdapter: BaseAdapter {
    var postList=ArrayList<Post>()
    var context:Context
    constructor(postList: ArrayList<Post>, context: Context){ //constructor receive list of post and context
        this.postList=postList
        this.context=context
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //separating data from list of post
        val userName=postList[position].userName
        val userImageUrl=postList[position].userImageUrl
        val recipeName=postList[position].recipeName
        val recipeDetails=postList[position].recipeDetails
        val recipeImageUrl=postList[position].recipeImageUrl
        //inflating a custom layout layout
        val view=LayoutInflater.from(context).inflate(R.layout.post_layout,null)
        //set values on view
        Glide.with(context).load(Uri.parse(userImageUrl)).into(view.card_userprofile_picture)
        view.card_username_text.setText(userName)
        Glide.with(context).load(recipeImageUrl).into(view.card_recipe_image)
        view.card_recipename_text.setText(recipeName)
        //on post click
        view.setOnClickListener{
            val intent=Intent(context,PostActivity::class.java)//sending to opened post activity
            val bundel=Bundle()
            bundel.putString("userName",userName)
            bundel.putString("userImageUrl",userImageUrl)
            bundel.putString("recipeName",recipeName)
            bundel.putString("recipeDetails",recipeDetails)
            bundel.putString("recipeImageUrl",recipeImageUrl)
            intent.putExtras(bundel)
            context.startActivity(intent)
        }
        return view
    }

    override fun getItem(position: Int): Any {//return current position on list of post
        return postList[position]
    }

    override fun getItemId(position: Int): Long {//return position as long
        return position.toLong()
    }

    override fun getCount(): Int {//return size of list
        return postList.size
    }
}