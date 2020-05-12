package com.myproject.sociorecipe

import com.google.firebase.storage.FirebaseStorage
import kotlin.properties.Delegates

class Post {
    //class to make object of post

    var id:Int=0
    lateinit var userName:String
    lateinit var userImageUrl:String
    lateinit var userImageByteArray: ByteArray
    lateinit var recipeName:String
    lateinit var recipeDetails:String
    lateinit var recipeImageUrl:String
    lateinit var recipeImageByteArray: ByteArray

    //overloaded constructors get different vales
    constructor(userName:String, userImageUrl:String, recipeName:String, recipeDetails:String, recipeImageUrl:String){
        this.userName=userName
        this.userImageUrl=userImageUrl
        this.recipeName=recipeName
        this.recipeDetails=recipeDetails
        this.recipeImageUrl=recipeImageUrl
    }
    constructor(recipeName:String, recipeDetails:String, recipeImageByteArray:ByteArray){
        this.recipeName=recipeName
        this.recipeDetails=recipeDetails
        this.recipeImageByteArray=recipeImageByteArray
    }
    constructor(id:Int, recipeName:String, recipeDetails:String, recipeImageByteArray:ByteArray){
        this.id=id
        this.recipeName=recipeName
        this.recipeDetails=recipeDetails
        this.recipeImageByteArray=recipeImageByteArray
    }
}