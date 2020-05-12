package com.myproject.sociorecipe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.fragment_post.*
import java.util.*


class PostFragment : Fragment() {
    lateinit var recipeNameView:EditText
    lateinit var recipeDetailsView:EditText
    lateinit var recipeImageView:ImageView
    lateinit var saveRecipeBtn:Button
    lateinit var recipeAnimationView:com.wang.avi.AVLoadingIndicatorView
    var selectedImage:Uri?= null
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var userImageName:String
    lateinit var userName:String
    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener
    lateinit var recipeName: String
    lateinit var recipeDetails: String
    lateinit var firebaseStorageReference: StorageReference
    lateinit var firebaseDatabaseReference: DatabaseReference
    var userLocation:Location?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_post, container, false)

        //firebase
        firebaseAuth=FirebaseAuth.getInstance()
        currentUser=firebaseAuth.currentUser!!
        firebaseStorageReference=FirebaseStorage.getInstance().reference
        firebaseDatabaseReference= FirebaseDatabase.getInstance().reference
        userName=currentUser.displayName!!
        userImageName=currentUser.uid

        //location Manager
        locationManager= layoutInflater.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //location listener to listen location updates
        locationListener=object :LocationListener{
            override fun onLocationChanged(location: Location?) {//if location change
                userLocation=location
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {//if GPS turn on
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0f,locationListener)
            }

            override fun onProviderDisabled(provider: String?) {//if GPS is off
                turnOnGps()
            }
        }


        //find views
        recipeNameView=view.findViewById(R.id.post_recipename_edit)
        recipeDetailsView=view.findViewById(R.id.post_recipedetailt_edit)
        recipeImageView=view.findViewById(R.id.post_recipe_image)
        saveRecipeBtn=view.findViewById(R.id.post_save_btn)
        recipeAnimationView=view.findViewById(R.id.post_loading_animation)

        //select image or request external storage permission
        recipeImageView.setOnClickListener {
            //check if the device api is greater than 22 and external storage permission not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(layoutInflater.context,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                //if device version is small than api 22 or permission granted
                pickImage()
            }
        }

        //save button click listener
        saveRecipeBtn.setOnClickListener {
            recipeName=recipeNameView.text.toString()
            recipeDetails=recipeDetailsView.text.toString()
            //validations
            if(selectedImage==null){
                TastyToast.makeText(activity!!.applicationContext,"Select recipe image.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (recipeName.length<=20){
                TastyToast.makeText(activity!!.applicationContext,"Recipe name should be greater than twenty characters.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (recipeDetails.length<=50){
                TastyToast.makeText(activity!!.applicationContext,"Recipe details should be greater than fifty characters.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(layoutInflater.context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),3)
                }else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    turnOnGps()
                }else{//validation successful
                    //invisible save btn and visible animation
                    saveRecipeBtn.visibility=View.INVISIBLE
                    recipeAnimationView.visibility=View.VISIBLE
                    //function call to get location
                    getLocation()
                }
            }
        }

        return view
    }

    //function to pick image from gallery
    private fun pickImage(){
        val intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //intent to pick image
        startActivityForResult(intent,2)
    }


    //override method for permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //check permission granted or not for storage
        if (requestCode==1){
            if(permissions[0]==android.Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                pickImage()
            }else{
                TastyToast.makeText(activity!!.applicationContext,"Please allow external storage permission.",TastyToast.LENGTH_LONG,TastyToast.CONFUSING).show()
            }
        }else if (requestCode==3){
            if (permissions[0]==Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){//if permission granted but gps is off
                    turnOnGps()
                }else{//permission granted and gps is on
                    saveRecipeBtn.visibility=View.INVISIBLE
                    recipeAnimationView.visibility=View.VISIBLE
                    getLocation()
                }
            }else{
                TastyToast.makeText(layoutInflater.context,"Location permission is required.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //image pick intent result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==2 && resultCode== Activity.RESULT_OK && data!=null){ //check request and data
            try {
                if (data.data!=null){
                    selectedImage = data.data!!//saving image uri to selected image variable
                    Glide.with(this).load(selectedImage).into(recipeImageView) //user glide library to sent image
                }
            }catch (ex:Exception){
                TastyToast.makeText(activity!!.applicationContext,"${ex.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //if GPS is off
    private fun turnOnGps(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        TastyToast.makeText(layoutInflater.context,"Turn on your GPS.",Toast.LENGTH_LONG,TastyToast.WARNING).show()
    }

    //get Location
    fun getLocation(){
        userLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)//get last known location
        //request location update and call location listener
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0f,locationListener)
        if (userLocation!=null){//location fund
            //pass location latitude and longitude to get country name function
            getCountryName(userLocation!!.latitude,userLocation!!.longitude)
        }else{//location not found
            TastyToast.makeText(layoutInflater.context,"Your location not found retry.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
            recipeAnimationView.visibility=View.INVISIBLE
            saveRecipeBtn.visibility=View.VISIBLE
        }
    }

    //get country name
    fun getCountryName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(layoutInflater.context, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)//get complete address of latitude and longitude
            if (addresses != null && !addresses.isEmpty()) {
               saveData(addresses[0].getCountryName())//get country name from address and pass to save data function
            }
        } catch (ex:Exception) {//address not found
            TastyToast.makeText(layoutInflater.context,"Your location not found retry.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
            recipeAnimationView.visibility=View.INVISIBLE
            saveRecipeBtn.visibility=View.VISIBLE
        }
    }

    //save data to firebase
    fun saveData(countryName:String){
        val randomImageName=UUID.randomUUID().toString() //random name for image
        val storagePath=firebaseStorageReference.child("Post Pictures/${randomImageName}")//firebase storage reference
        //upload image
        storagePath.putFile(selectedImage!!).addOnCompleteListener {
            if (it.isSuccessful){//image upload successful
                //get image uri from firebase database
                storagePath.downloadUrl.addOnSuccessListener {
                    //save post to database
                    val randomPostName=UUID.randomUUID().toString()
                    firebaseDatabaseReference.child("Post").child(randomPostName).child("username").setValue(userName)
                    firebaseDatabaseReference.child("Post").child(randomPostName).child("userImageName").setValue(userImageName)
                    firebaseDatabaseReference.child("Post").child(randomPostName).child("recipeName").setValue(recipeName)
                    firebaseDatabaseReference.child("Post").child(randomPostName).child("recipeDetails").setValue(recipeDetails)
                    firebaseDatabaseReference.child("Post").child(randomPostName).child("recipeImageUrl").setValue(it.toString())
                    firebaseDatabaseReference.child("Post").child(randomPostName).child("countryName").setValue(countryName)
                    //animation invisible....
                    saveRecipeBtn.visibility=View.VISIBLE
                    recipeAnimationView.visibility=View.INVISIBLE
                    TastyToast.makeText(layoutInflater.context,"Recipe shared.",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show()
                    //sending user back to home
                    val intent=Intent(layoutInflater.context,MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }.addOnFailureListener {
            //image upload failed
            saveRecipeBtn.visibility=View.VISIBLE
            recipeAnimationView.visibility=View.INVISIBLE
            TastyToast.makeText(layoutInflater.context,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }

}
