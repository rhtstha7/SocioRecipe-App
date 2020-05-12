package com.myproject.sociorecipe

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class SaveOffline {
    var context:Context
    var recipeName:String
    var recipeDetails:String
    var recipeImage:Bitmap
    constructor(context: Context, recipeName:String, recipeDetails:String, recipeImage:Bitmap){//receiving data from download class
        this.context=context
        this.recipeName=recipeName
        this.recipeDetails=recipeDetails
        this.recipeImage=recipeImage
    }
    fun saveData(){//creating object of post and sending that object to sqlite helper class
        val byteArrayOutputStream=ByteArrayOutputStream()
        recipeImage.compress(Bitmap.CompressFormat.JPEG, 50,byteArrayOutputStream)
        val byteArray=byteArrayOutputStream.toByteArray()
        val post=Post(recipeName,recipeDetails,byteArray)
        val sqliteDatabase=SqliteDatabase(context)
        sqliteDatabase.savePost(post)
    }
}