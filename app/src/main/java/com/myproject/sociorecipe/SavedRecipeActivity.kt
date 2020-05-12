package com.myproject.sociorecipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_saved_recipe.*

class SavedRecipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_recipe)
        val postlist=SqliteDatabase(this).readData()//reading data from sqlite database
        if (postlist.size<=0){//if size of list recived from sqlite equal zero hide list view and show a text
            saved_recipe_comment.visibility=View.VISIBLE
            saved_post_list.visibility=View.INVISIBLE
        }
        //setting adapter
        val savedPostAdapter=SavedPostAdapter(postlist,this)
        saved_post_list.adapter=savedPostAdapter
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
