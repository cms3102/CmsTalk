package com.csergio.cmstalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.csergio.cmstalk.fragment.PeopleFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainActivity_frameLayout, PeopleFragment())
            .commit()


    }
}
