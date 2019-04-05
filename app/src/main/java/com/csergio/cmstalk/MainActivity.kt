package com.csergio.cmstalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.csergio.cmstalk.fragment.AccountFragment
import com.csergio.cmstalk.fragment.ChatFragment
import com.csergio.cmstalk.fragment.PeopleFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
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
                R.id.action_account -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainActivity_frameLayout, AccountFragment())
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

        setPushToken()

    }

    private fun setPushToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val token = FirebaseInstanceId.getInstance().getToken()
        val map = mutableMapOf<String, Any?>()
        map["pushToken"] = token
        // 내 계정 정보에 서버 푸시를 위한 pushToken 항목 추가. setValue로 하면 기존 데이터 다 사라지니 쓰지 않도록 주의 필요
        FirebaseDatabase.getInstance().getReference("users/$uid").updateChildren(map)
    }
}
