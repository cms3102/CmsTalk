package com.csergio.cmstalk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.csergio.cmstalk.model.UserModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var splashBackground:String
    private var imageUri: Uri = Uri.parse("")
    private val PICK_FROM_ALBUM: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 프로필 이미지 이벤트 처리 설정
        signUpActivity_ImageView_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
        }

        remoteConfig = FirebaseRemoteConfig.getInstance()
        splashBackground = remoteConfig.getString(getString(R.string.splash_background))
        // 폰 화면 상단 상태 바 색깔 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(splashBackground)
        }
        signUpActivity_button_signUp.setBackgroundColor(Color.parseColor(splashBackground))

        // 회원 가입 처리
        signUpActivity_button_signUp.setOnClickListener {

            if(signUpActivity_editText_email.text.toString() == null || signUpActivity_editText_name.text.toString() == null || signUpActivity_editText_password.text.toString() == null){
                Toast.makeText(this, "입력 내용을 확인해 주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(signUpActivity_editText_email.text.toString(), signUpActivity_editText_password.text.toString())
                .addOnCompleteListener(this, OnCompleteListener {

                    val uid = it.result?.user?.uid.toString()

                    // 프로필 이미지 선택했을 때
                    if (imageUri.toString() != ""){
                        val ref = FirebaseStorage.getInstance().reference.child("userImages/$uid")
                        val uploadTask = ref.putFile(imageUri)
                        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            return@Continuation ref.downloadUrl
                        }).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result.toString()
                                val userModel = UserModel()
                                    userModel.uid = uid
                                    userModel.userName = signUpActivity_editText_name.text.toString()
                                    userModel.profileImageUri = downloadUri
                                    FirebaseDatabase.getInstance().getReference("users/$uid").setValue(userModel)
                                Toast.makeText(this, "이름, 아이디, 사진 DB 저장됨", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this, "실패", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // 프로필 이미지 선택 안 했을 때
                        val userModel = UserModel()
                        userModel.uid = uid
                        userModel.userName = signUpActivity_editText_name.text.toString()
                        FirebaseDatabase.getInstance().getReference("users/$uid").setValue(userModel)
                        Toast.makeText(this, "이름, 아이디 DB 저장됨", Toast.LENGTH_LONG).show()
                        finish()
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 선택된 프로필 이미지 출력
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            imageUri = data?.data!!
            signUpActivity_ImageView_profile.setImageURI(imageUri)
        }
    }

}
