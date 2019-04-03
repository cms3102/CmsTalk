package com.csergio.cmstalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.csergio.cmstalk.fragment.ChatFragment
import com.csergio.cmstalk.fragment.PeopleFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivity_bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.action_people -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainActivity_frameLayout, PeopleFragment())
                        .commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_chat -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainActivity_frameLayout, ChatFragment())
                        .commit()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainActivity_frameLayout, PeopleFragment())
            .commit()

    }
}
