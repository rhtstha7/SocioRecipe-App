package com.myproject.sociorecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sdsmdg.tastytoast.TastyToast


class SearchFragment : Fragment() {
    lateinit var searchView:SearchView
    lateinit var listView:ListView
    var postList=ArrayList<Post>()
    lateinit var postAdapter:PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_search, container, false)

        //find view
        searchView=view.findViewById(R.id.search_search_edit)
        listView=view.findViewById(R.id.search_post_list)

        postAdapter=PostAdapter(postList,layoutInflater.context) //object of post adapter
        listView.adapter=postAdapter //set list view adapter

        //search query listener
        searchView.setOnQueryTextListener(object :OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchData(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchData(newText)
                return true
            }

        })

        return view
    }

    //function get searched data from firebase
    fun searchData(query:String?){
        postList.clear()
        postAdapter.notifyDataSetChanged()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Post")
        val query=databaseReference.orderByChild("recipeName").startAt(query).endAt(query+"\uf8ff")//firebase query to search data

        query.addValueEventListener(object:ValueEventListener{//value event listener
            //on error occurred
            override fun onCancelled(p0: DatabaseError) {
                TastyToast.makeText(layoutInflater.context,"${p0.toException().localizedMessage}", TastyToast.LENGTH_LONG, TastyToast.ERROR).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                //loop on each child of post table in firebase DB
                p0.children.forEach {
                    val hashMap=it.value as HashMap<String,String> //covert data snapshot value to hashmap
                    if (hashMap.size>0){//check if hashmap size is greater than 0
                        val userName=hashMap["username"]
                        val userImageName=hashMap["userImageName"]
                        val recipeName=hashMap["recipeName"]
                        val recipeDetails=hashMap["recipeDetails"]
                        val recipeImageUrl=hashMap["recipeImageUrl"]
                        if (userName!=null && userImageName!=null && recipeName!=null && recipeDetails!=null && recipeImageUrl!=null){
                            FirebaseStorage.getInstance().reference.child("Profile Pictures/$userImageName").downloadUrl.addOnSuccessListener {
                                //if link retrieve successfully
                                val userImageUrl=it.toString()//convert uri to string to get url
                                makeView(userName, userImageUrl, recipeName, recipeDetails, recipeImageUrl)//call make view function
                            }
                        }
                    }
                }
            }
        })
    }

    //collect data of snapshot and add in post Array list of post
    fun makeView(userName:String, userImageUrl:String, recipeName:String, recipeDetails:String,recipeImageUrl:String){
        val myPost=Post(userName, userImageUrl, recipeName, recipeDetails,recipeImageUrl)
        postList.add(myPost)
        postAdapter.notifyDataSetChanged()
    }
}
