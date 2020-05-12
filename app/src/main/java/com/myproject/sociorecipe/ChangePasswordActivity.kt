package com.myproject.sociorecipe

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Credentials
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_signup.*

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var currentUser:FirebaseUser
    lateinit var authCredentials: AuthCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        firebaseAuth=FirebaseAuth.getInstance() //firebase auth instance
        currentUser=firebaseAuth.currentUser!! //current login user

        changepassword_save_btn.setOnClickListener {
            val oldPassword=changepassword_oldpassword_edit.text.toString() //old password text
            val newPassword=changepassword_newpassword_edit.text.toString() //new password text
            //validation
            if (oldPassword.length<=0){
                TastyToast.makeText(this,"Enter old password.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (oldPassword.length<6){
                TastyToast.makeText(this,"Old Password must be at least 6 characters.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (newPassword.length<=0){
                TastyToast.makeText(this,"Enter new password.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (newPassword.length<6){
                TastyToast.makeText(this,"New password must be at least 6 characters.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else{ //validation success
                //visible animation and unvisible button
                changepassword_loading_animation.visibility=View.VISIBLE
                changepassword_save_btn.visibility=View.INVISIBLE
                authCredentials=EmailAuthProvider.getCredential(currentUser.email!!,oldPassword) //reassessing auth credentials to change password
                //reauth current user
                currentUser.reauthenticate(authCredentials).addOnCompleteListener {
                    if (it.isSuccessful){ //on auth successful
                        //update password request complete
                        currentUser.updatePassword(newPassword).addOnCompleteListener {
                            if (it.isSuccessful){//password updated successfully
                                TastyToast.makeText(this,"Password changed.",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show()
                                onBackPressed()//go back to profile activity
                                //make animation invisible and button visible
                                changepassword_loading_animation.visibility=View.INVISIBLE
                                changepassword_save_btn.visibility=View.VISIBLE
                            }
                        }.addOnFailureListener {
                            //update password request failed
                            TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
                            changepassword_loading_animation.visibility=View.INVISIBLE
                            changepassword_save_btn.visibility=View.VISIBLE
                        }
                    }
                }.addOnFailureListener {
                    //user auth failed
                    TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
                    changepassword_loading_animation.visibility=View.INVISIBLE
                    changepassword_save_btn.visibility=View.VISIBLE
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
