package com.myproject.sociorecipe

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.myproject.sociorecipe.Post
import com.sdsmdg.tastytoast.TastyToast

val dbName="MyPostDB"
val tableName="MyPostTable"
val version=1
var post_id="id"
var post_name="name"
var post_details="details"
var post_image="image"

class SqliteDatabase: SQLiteOpenHelper{
    var context:Context

    constructor(context: Context):super(context, dbName, null, version){ // getting context as parameter in constructor
        this.context=context
    }
    // sqlite helper override method || on database create
    override fun onCreate(db: SQLiteDatabase?) {
        //create table to save post
        val createTable= "CREATE TABLE IF NOT EXISTS $tableName($post_id INTEGER PRIMARY KEY AUTOINCREMENT, $post_name VARCHAR, $post_details VARCHAR, $post_image BLOB)"
        db!!.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //to save post in database
    fun savePost(post: Post){ //getting post object of Post class as parameter
        val previousSaved=readData() //reading previous data to check this post already saved or not
        if (previousSaved.size<=0){ //size 0 or less means nothing saved
            addPostInDb(post) //saving post
        }else{
            for (i in 0..previousSaved.size-1){ //loop on list
                if (previousSaved[i].recipeName!=post.recipeName && previousSaved[i].recipeDetails!=post.recipeDetails){//if name and details both don't match
                    if (i==previousSaved.size-1){//and courser reach on last location of list
                        addPostInDb(post) //save post
                    }
                }else if (i==previousSaved.size-1){//post match
                    TastyToast.makeText(context,"Post already saved.",TastyToast.LENGTH_LONG,TastyToast.INFO).show()
                }
            }
        }
    }


    //add post in db
    private fun addPostInDb(post:Post){
        var result=-1
        try {
            //separating value
            val name=post.recipeName
            val details=post.recipeDetails
            val image=post.recipeImageByteArray
            val db=this.writableDatabase
            val cv= ContentValues() //content value to put data in database
            cv.put(post_name,name)
            cv.put(post_details,details)
            cv.put(post_image,image)
            result=db.insert(tableName, null, cv).toInt()
            db.close()
        }catch (ex:Exception){
            TastyToast.makeText(context,"${ex.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
        if (result==-1){
            TastyToast.makeText(context,"Something is wrong.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }else{
            TastyToast.makeText(context,"Post saved.",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show()
        }
    }

    //to read data from database return ArrayList of post objects of post class
    fun readData():ArrayList<Post>{
        val postList=ArrayList<Post>()
        try {
            val db=this.readableDatabase
            val courser=db.rawQuery("SELECT * FROM $tableName",null) //saving courser returned from select query
            courser.moveToFirst() //moving courser to first row of table
            while (courser!=null){
                // separating data by column index
                var id=courser.getInt(courser.getColumnIndex(post_id))
                var name=courser.getString(courser.getColumnIndex(post_name))
                var details=courser.getString(courser.getColumnIndex(post_details))
                var image=courser.getBlob(courser.getColumnIndex(post_image))
                //saving separated data in post object
                val post=Post(id,name,details,image)
                postList.add(post) // adding post object to postlist(ArrayList of post)
                courser.moveToNext() // moving courser to next row
            }
            courser?.close()
            db.close()
        }catch (ex:Exception) {
            ex.localizedMessage
        }
        return postList
    }
}