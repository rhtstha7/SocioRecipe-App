package com.myproject.sociorecipe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_post.*
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HomeFragment : Fragment() {
    lateinit var listView:ListView
    lateinit var homeAnimation:com.wang.avi.AVLoadingIndicatorView
    lateinit var firebaseDatabaseReference: DatabaseReference
    lateinit var postAdapter:PostAdapter
    var postList=ArrayList<Post>()
    var userLocation: Location?=null
    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_home, container, false)
        //find view
        listView=view.findViewById(R.id.home_post_list)
        homeAnimation=view.findViewById(R.id.home_loading_animation)

        //call location manager function on create
        locationManager()

        postAdapter=PostAdapter(postList,layoutInflater.context)//object of PostAdapter
        listView.adapter=postAdapter //set list view adapter



        //if location permission is not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(layoutInflater.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),3)
        }else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //if permission granted but gps is off call turn on gps function
            turnOnGps()
        }else{//validation successful
            //function call to get location
            getLocation()
        }

        return view
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
        if (userLocation!=null){//location found
            //pass location latitude and longitude to get country name function
            getCountryName(userLocation!!.latitude,userLocation!!.longitude)
        }else{//location not found
            TastyToast.makeText(layoutInflater.context,"Your location not found retry.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }

    fun locationManager(){
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
    }

    //get country name
    fun getCountryName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(layoutInflater.context, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)//get complete address of latitude and longitude
            if (addresses != null && !addresses.isEmpty()) {
                //firebase
                firebaseDatabaseReference=FirebaseDatabase.getInstance().reference
                getDataFromFirebase(addresses[0].getCountryName())
            }
        } catch (ex:Exception) {//address not found
            TastyToast.makeText(layoutInflater.context,"Your location not found retry.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
        }
    }


    //function get data firebase
    fun getDataFromFirebase(countryName:String){
        //value event listener
        firebaseDatabaseReference.child("Post").addValueEventListener(object:ValueEventListener{
            //on error occurred
            override fun onCancelled(p0: DatabaseError) {
                TastyToast.makeText(layoutInflater.context,"${p0.toException().localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
            }


            override fun onDataChange(p0: DataSnapshot) {
               for (data in p0.children){//loop on all child of post
                   val hashMap=data.value as HashMap<String,String> //covert data snapshot value to hashmap
                   if (hashMap.size>0){//check if hashmap size is greater than 0
                       if (hashMap["countryName"]==countryName){//get data if country name match
                           val userName=hashMap["username"]
                           var userImageName=hashMap["userImageName"]
                           val recipeName=hashMap["recipeName"]
                           val recipeDetails=hashMap["recipeDetails"]
                           val recipeImageUrl=hashMap["recipeImageUrl"]
                           if (userName!=null && userImageName!=null && recipeName!=null && recipeDetails!=null && recipeImageUrl!=null){ //get user image url if all data not null
                               //if link retrieve successfully
                               FirebaseStorage.getInstance().reference.child("Profile Pictures/$userImageName").downloadUrl.addOnSuccessListener {
                                   val userImageUrl=it.toString()//convert uri to string to get url
                                   makeView(userName, userImageUrl, recipeName, recipeDetails, recipeImageUrl)//call make view function
                               }
                           }
                       }
                   }
               }
            }
        })
    }

    fun makeView(userName:String, userImageUrl:String, recipeName:String, recipeDetails:String,recipeImageUrl:String){
        //collect data of snapshot and add in post Array list of post
        val myPost=Post(userName, userImageUrl, recipeName, recipeDetails,recipeImageUrl)
        postList.add(myPost)
        postAdapter.notifyDataSetChanged()
        listView.visibility=View.VISIBLE
        homeAnimation.visibility=View.INVISIBLE
    }

    override fun onResume() {
        //call location manager function on resume
        locationManager()
        super.onResume()
    }
}
