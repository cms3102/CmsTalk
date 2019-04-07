package com.csergio.cmstalk

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private lateinit var remoteConfig:FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.default_config)

        remoteConfig.fetch(0)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
//                    Toast.makeText(this, "Fetch Succeeded",
//                        Toast.LENGTH_SHORT).show()
                    remoteConfig.activateFetched()
                } else {
//                    Toast.makeText(this, "Fetch Failed",
//                        Toast.LENGTH_SHORT).show()
                }
                displayWelcomeMessage()
            }
    }

    private fun displayWelcomeMessage() {
        val splashBackground = remoteConfig.getString(getString(R.string.splash_background))
        val caps = remoteConfig.getBoolean("splash_message_caps")
        val splashMessage = remoteConfig.getString("splash_message")

        splash_ConstraintLayout.setBackgroundColor(Color.parseColor(splashBackground))

        if (caps){
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(splashMessage).setPositiveButton("확인", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    finish()
                }
            })
            alertDialogBuilder.create().show()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
