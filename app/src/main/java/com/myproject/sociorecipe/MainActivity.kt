package com.myproject.sociorecipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    lateinit var fragment:Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //bottom navigation item selected listener passed  this as main activity implemented
        bottom_navigation.setOnNavigationItemSelectedListener(this)

        //on start default fragment
        if (savedInstanceState==null){
            fragment=HomeFragment()
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
        }
    }

    override fun onBackPressed() {
        finishAffinity()//close everything
    }

    //override method navigation item select listener
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){//change fragment on selected
            R.id.menu_home->{
                fragment=HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
            }
            R.id.menu_search->{
                fragment=SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
            }
            R.id.menu_add_post->{
                fragment=PostFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
            }
            R.id.menu_profile->{
                fragment=ProfileFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
            }
        }
        return true
    }
}
