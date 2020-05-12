package com.myproject.sociorecipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.activity_saved_post.*

//activity of opened post from saved post
class SavedPostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_post)
        //getting data from bundle and changing values of view
        val bundle=intent.extras
        val recipeName=bundle?.getString("recipeName")
        val recipeDetails=bundle?.getString("recipeDetails")
        val recipeImage=bundle?.getByteArray("recipeImage")
        saved_post_activity_name.setText(recipeName)
        saved_post_activity_details.setText(recipeDetails)
        Glide.with(this).load(recipeImage).into(saved_post_activity_image)
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
