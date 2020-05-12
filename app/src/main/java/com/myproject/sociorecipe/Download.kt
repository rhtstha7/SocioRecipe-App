package com.myproject.sociorecipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.View
import android.widget.Button
import com.sdsmdg.tastytoast.TastyToast
import java.io.ByteArrayOutputStream
import java.net.URL

class Download: AsyncTask<String, Unit, Bitmap> {//implement asynctask to download image
    var animationView:com.wang.avi.AVLoadingIndicatorView
    var saveButton: Button
    var context: Context
    var imageUrl:String
    var recipeName:String
    var recipeDetails:String

    //get all data to of post, animation view and save button in constructor to download data from firebase and save offline
    constructor(animationView:com.wang.avi.AVLoadingIndicatorView, saveButton:Button, context: Context, imageUrl:String, recipeName:String, recipeDetails:String){
        this.animationView=animationView
        this.saveButton=saveButton
        this.context=context
        this.imageUrl=imageUrl
        this.recipeName=recipeName
        this.recipeDetails=recipeDetails
    }

    //before asynctask started make animation visible and button invisible
    override fun onPreExecute() {
        animationView.visibility= View.VISIBLE
        saveButton.visibility=View.INVISIBLE
        super.onPreExecute()
    }

    //perform downloading task in background
    override fun doInBackground(vararg params: String?): Bitmap? {
        var bitmap:Bitmap?=null
        try {//get image from url and save as bitmap
            val inputStream= URL(imageUrl).openStream()
            bitmap= BitmapFactory.decodeStream(inputStream)
        }catch (ex:Exception){
            TastyToast.makeText(context,"${ex.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
        return bitmap
    }

    //after downloading
    override fun onPostExecute(result: Bitmap?) {
        //make animation invisible and button visible
        animationView.visibility=View.INVISIBLE
        saveButton.visibility=View.VISIBLE
        if (result!=null){//if found result as bitmap
            val saveOffline=SaveOffline(context, recipeName, recipeDetails, result)//object of SaveOffline class to perform save operation
            saveOffline.saveData()
        }else{
            TastyToast.makeText(context,"Some thing is wrong, retry.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
        super.onPostExecute(result)
    }
}