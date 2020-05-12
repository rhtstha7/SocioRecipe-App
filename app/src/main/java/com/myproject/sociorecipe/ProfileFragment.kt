package com.myproject.sociorecipe

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.ViewUtils
import androidx.core.content.ContextCompat.checkSelfPermission
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var usernameView:TextView
    lateinit var emailView:TextView
    lateinit var profilePictureLayout: FrameLayout
    lateinit var profilePictureView: de.hdodenhof.circleimageview.CircleImageView
    lateinit var changePasswordView:Button
    lateinit var savedRecipesView:Button
    lateinit var signoutView:Button
    lateinit var imageUri:Uri
    lateinit var animationView:com.wang.avi.AVLoadingIndicatorView
    lateinit var plusBtnView:de.hdodenhof.circleimageview.CircleImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_profile, container, false)

        //get uri of default image
        imageUri=Uri.parse("android.resource://com.myproject.sociorecipe/drawable/deafult_profile")

        firebaseAuth=FirebaseAuth.getInstance() //firebase instance
        currentUser=firebaseAuth.currentUser!! //get current user
        //current user details
        val username=currentUser.displayName
        val email=currentUser.email
        val imageUrl=currentUser.photoUrl.toString()

        //getting views from view
        profilePictureLayout=view.findViewById(R.id.profile_user_imageview)
        profilePictureView=view.findViewById(R.id.profile_user_image)
        usernameView=view.findViewById(R.id.profile_username_text)
        emailView=view.findViewById(R.id.profile_email_text)
        changePasswordView=view.findViewById(R.id.profile_changepassword_btn)
        savedRecipesView=view.findViewById(R.id.profile_savedrecipes_btn)
        signoutView=view.findViewById(R.id.profile_signout_btn)
        animationView=view.findViewById(R.id.profile_loading_animation)
        plusBtnView=view.findViewById(R.id.profile_plus_btn)

        //display current user details
        if (username!=null){
            usernameView.setText(username)
        }else{
            usernameView.setText("Username here")
        }
        if (email!=null){
            emailView.setText(email)
        }else{
            emailView.setText("Email here")
        }
        if (imageUrl!=null){
            Glide.with(this).load(imageUrl).into(profilePictureView)
        }else{
            Glide.with(this).load(imageUri).into(profilePictureView)
        }

        //profile image click listener
        profilePictureLayout.setOnClickListener {
            //check if the device api is greater than 22 and external storage permission not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(activity!!.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                //if device version is small than api 22 or permission granted
                pickImage()
            }
        }

        //change password click listener
        changePasswordView.setOnClickListener {
            val intent=Intent(activity?.applicationContext,ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        //saved recipes click listener
        savedRecipesView.setOnClickListener {
            val intent=Intent(activity?.applicationContext,SavedRecipeActivity::class.java)
            startActivity(intent)
        }

        //signout click listener
        signoutView.setOnClickListener {
            firebaseAuth.signOut()//firebase auth singout funcation
            //sending back to login activity
            val intent=Intent(activity?.applicationContext,SigninActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }

    //override method for permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //check permission granted or not
        if (requestCode==1 && permissions[0]==android.Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            pickImage()
        }else{
            TastyToast.makeText(activity!!.applicationContext,"Please allow external storage permission.",TastyToast.LENGTH_LONG,TastyToast.CONFUSING).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //image pick intent result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==2 && resultCode==Activity.RESULT_OK && data!=null){ //check request and data
            try {
                if (data.data!=null){//if data not null, image picked
                    imageUri = data.data!!
                    profilePictureView.visibility=View.INVISIBLE
                    plusBtnView.visibility=View.INVISIBLE
                    animationView.visibility=View.VISIBLE
                    uploadImage()//call upload image funcation
                }
            }catch (ex:Exception){
                TastyToast.makeText(activity!!.applicationContext,"${ex.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    //function to pick image from gallery
    private fun pickImage(){
        val intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,2)
    }
    private fun uploadImage(){
        //getting storage refreance with complete path
        val firebaseStorageReference= FirebaseStorage.getInstance().getReference().child("Profile Pictures/${currentUser.uid}")
        //uploading file
        firebaseStorageReference.putFile(imageUri).addOnCompleteListener{
            if (it.isSuccessful){
                firebaseStorageReference.downloadUrl.addOnSuccessListener {
                    val imageDownloadUrl=it.toString()//converting uri to url
                    saveUrl(imageDownloadUrl)//calling save url to
                }
            }
        }.addOnFailureListener{
            //uploading failed
            profilePictureView.visibility=View.VISIBLE
            plusBtnView.visibility=View.VISIBLE
            animationView.visibility=View.INVISIBLE
            TastyToast.makeText(activity!!.applicationContext,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }
    private fun saveUrl(imageDownloadUrl:String){
        val userProfileChangeRequest= UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(imageDownloadUrl))//updating profile image
            .build()
        //update profile listener
        currentUser.updateProfile(userProfileChangeRequest).addOnCompleteListener{
            if (it.isSuccessful){
                profilePictureView.visibility=View.VISIBLE
                plusBtnView.visibility=View.VISIBLE
                animationView.visibility=View.INVISIBLE
                //changing image on view
                Glide.with(this).load(imageUri).into(profilePictureView) //user glide library to sent image
            }
        }.addOnFailureListener {
            //uploading failed
            profilePictureView.visibility=View.VISIBLE
            plusBtnView.visibility=View.VISIBLE
            animationView.visibility=View.INVISIBLE
            TastyToast.makeText(activity!!.applicationContext,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }
}
