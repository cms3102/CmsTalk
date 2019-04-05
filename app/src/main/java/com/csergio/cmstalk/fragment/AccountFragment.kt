package com.csergio.cmstalk.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.csergio.cmstalk.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_comment.view.*
import kotlinx.android.synthetic.main.fragment_account.view.*

class AccountFragment:Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutView = inflater.inflate(R.layout.fragment_account, container, false)

        layoutView.accountFragment_button_comment.setOnClickListener {
            showDialog(layoutView.context)
        }

        return layoutView
    }

    fun showDialog(context:Context){

        val alertDialogBuilder = AlertDialog.Builder(context)
        val view = layoutInflater?.inflate(R.layout.dialog_comment, null)

        alertDialogBuilder.setView(view).setPositiveButton("확인", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val stringObjectMap = mutableMapOf<String, Any>()
                stringObjectMap["statusMessage"] = view.commentDialog_editText.text.toString()
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                FirebaseDatabase.getInstance().getReference("users/$uid").updateChildren(stringObjectMap)
            }

        }).setNegativeButton("취소", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
            }

        })

        alertDialogBuilder.show()
    }
}