package com.myproject.sociorecipe

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_signup.*


class SignupActivity : AppCompatActivity() {
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var imageUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        //get uri of default image
        imageUri=Uri.parse("android.resource://com.myproject.sociorecipe/drawable/deafult_profile")
        //firebase instance
        firebaseAuth=FirebaseAuth.getInstance()

        //underline text
        signup_login_btn.paint.isUnderlineText=true

        //login click listener
        signup_login_btn.setOnClickListener {
            onBackPressed() //call onBackPressed function
        }

        //profile picture click listener
        signup_profile_imageview.setOnClickListener{
            //check if the device api is greater than 22 and external storage permission not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                //if device version is small than api 22 or permission granted
                pickImage()
            }
        }

        //sign up click listener
        signup_signup_btn.setOnClickListener {
            val username=signup_username_edit.text.toString()
            val email= signup_email_edit.text.toString()
            val password= signup_password_edit.text.toString()
            //validation
            if (username.length<=0){
                TastyToast.makeText(this,"Enter username.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (email.length<=0){
                TastyToast.makeText(this,"Enter email.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                TastyToast.makeText(this,"Enter valid email.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (password.length<=0){
                TastyToast.makeText(this,"Enter password.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (password.length<6){
                TastyToast.makeText(this,"Password must be at least 6 characters.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else{ //validation success

                signup_signup_btn.visibility=View.INVISIBLE
                signup_loading_animation.visibility= View.VISIBLE
                //create user with email and password and call next method upload profile picture
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful) {
                        uploadImage(firebaseAuth.currentUser!!, username) //upload profile picture
                    }
                }.addOnFailureListener {
                    TastyToast.makeText(this, "${it.localizedMessage}", Toast.LENGTH_LONG,TastyToast.ERROR).show()
                }
            }
        }
    }

    //override method for permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //check permission granted or not
        if (requestCode==1 && permissions[0]==android.Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            pickImage()
        }else{
            TastyToast.makeText(this,"Please allow external storage permission.",TastyToast.LENGTH_LONG,TastyToast.CONFUSING).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //image pick intent result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==2 && resultCode==Activity.RESULT_OK && data!=null){ //check request and data
            try {
                if (data.data!=null){
                    imageUri = data.data!!
                    Glide.with(this).load(imageUri).into(signup_profile_image) //user glide library to sent image
                }
            }catch (ex:Exception){
                TastyToast.makeText(this,"${ex.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //sending verification email and moving to login activity
    private fun sendVerificationEmail(currentUser:FirebaseUser){
        currentUser.sendEmailVerification().addOnCompleteListener{
            if (it.isSuccessful){
                TastyToast.makeText(this,"Account created, please verify your email.",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show()
                signup_loading_animation.visibility=View.INVISIBLE
                signup_signup_btn.visibility=View.VISIBLE
                onBackPressed()
            }
        }.addOnFailureListener {
            TastyToast.makeText(this, "${it.localizedMessage}", Toast.LENGTH_LONG,TastyToast.ERROR).show()
            signup_loading_animation.visibility=View.INVISIBLE
            signup_signup_btn.visibility=View.VISIBLE
            onBackPressed()
        }
    }

    //saving user details and calling next method of sending verification email
    private fun saverUserDetails(currentUser: FirebaseUser, username:String, imageDownloadUrl:String){
        val userProfileChangeRequest=UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .setPhotoUri(Uri.parse(imageDownloadUrl))
            .build()
        currentUser.updateProfile(userProfileChangeRequest).addOnCompleteListener{
            if (it.isSuccessful){
                sendVerificationEmail(currentUser)
            }
        }.addOnFailureListener {
            TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }

    //upload user image to firebase storage and calling next method to save user details
    private fun uploadImage(currentUser: FirebaseUser, username: String){
        val firebaseStorageReference=FirebaseStorage.getInstance().getReference().child("Profile Pictures/${currentUser.uid}")
        firebaseStorageReference.putFile(imageUri).addOnCompleteListener{
            if (it.isSuccessful){
                firebaseStorageReference.downloadUrl.addOnSuccessListener {
                    val imageDownloadUrl=it.toString()
                    saverUserDetails(currentUser,username,imageDownloadUrl)
                }
            }
        }.addOnFailureListener{
            TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }

    //function to pick image from gallery
    private fun pickImage(){
        val intent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,2)
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
