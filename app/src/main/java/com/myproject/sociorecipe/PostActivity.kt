package com.myproject.sociorecipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_post.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

//activity of opened post
class PostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        //receive bundle extras from intent
        val bundle=intent.extras!!
        //separating data display on views
        val userName=bundle.getString("userName")
        val userImageUrl=bundle.getString("userImageUrl")
        val recipeName=bundle.getString("recipeName")
        val recipeDetails=bundle.getString("recipeDetails")
        val recipeImageUrl=bundle.getString("recipeImageUrl")
        Glide.with(this).load(recipeImageUrl).into(postImage)
        postName.setText(recipeName)
        postDetails.setText(recipeDetails)
        Glide.with(this).load(userImageUrl).into(userImage)
        userNameText.setText(userName)

        postSave.setOnClickListener {
            Download(postLoading,postSave,this,recipeImageUrl!!, recipeName!!, recipeDetails!!).execute()
        }

    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

}
