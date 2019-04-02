package com.csergio.cmstalk

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val splashBackground = remoteConfig.getString(getString(R.string.splash_background))
        // 폰 화면 상단 상태 바 색깔 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(splashBackground)
        }
        loginActivity_button_login.setBackgroundColor(Color.parseColor(splashBackground))
        loginActivity_button_signUp.setBackgroundColor(Color.parseColor(splashBackground))


        loginActivity_button_login.setOnClickListener {
            loginEvent()
        }

        loginActivity_button_signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 로그인 인터페이스 리스너
        authStateListener = FirebaseAuth.AuthStateListener {
            val user = firebaseAuth.currentUser
            if (user != null){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {

            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    fun loginEvent() {
        firebaseAuth.signInWithEmailAndPassword(loginActivity_editText_id.text.toString(), loginActivity_editText_password.text.toString()).addOnCompleteListener {
            // 로그인 실패 시 메세지 띄우기
            if (!it.isSuccessful){
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
