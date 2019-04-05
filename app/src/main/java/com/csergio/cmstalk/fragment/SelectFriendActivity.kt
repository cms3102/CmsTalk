package com.csergio.cmstalk.fragment

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.csergio.cmstalk.R
import com.csergio.cmstalk.chat.MessageActivity
import com.csergio.cmstalk.model.ChatRoomModel
import com.csergio.cmstalk.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_select_friend.*
import kotlinx.android.synthetic.main.item_friend.view.*
import kotlinx.android.synthetic.main.item_friend_selected.view.*

class SelectFriendActivity : AppCompatActivity() {

    private val chatRoomModel = ChatRoomModel()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friend)

        selectFriendActivity_recyclerView.layoutManager = LinearLayoutManager(this)
        selectFriendActivity_recyclerView.adapter = SelectFragmentAdapter()

        selectFriendAtivity_button.setOnClickListener {
            chatRoomModel.members[myUid.toString()] = true
            FirebaseDatabase.getInstance().getReference("chatRooms").push().setValue(chatRoomModel)
        }
    }

    inner class SelectFragmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.item_friend_selected_imageView
        val textView = itemView.item_friend_selected_textView
        val textView_statusMessage = itemView.item_friend_selected_textView_comment
        val checkBox = itemView.item_friend_selected_checkBox
    }

    inner class SelectFragmentAdapter: RecyclerView.Adapter<SelectFragmentViewHolder>{

        val userModels = mutableListOf<UserModel>()

        constructor(){
            FirebaseDatabase.getInstance().getReference("users").addValueEventListener(object : ValueEventListener {

                override fun onCancelled(databaseError: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userModels.clear()
                    for (snapshot in dataSnapshot.children){
                        val userModel = snapshot.getValue(UserModel::class.java)
                        userModel?.let {
                            // 내 데이터는 친구 목록에 안 나오게 처리
                            if (userModel.uid != myUid && userModel.uid != ""){
                                userModels.add(userModel)
                            }
                        }
                    }
                    notifyDataSetChanged()
                }

            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectFragmentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_selected, parent, false)
            return SelectFragmentViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userModels.size
        }

        override fun onBindViewHolder(holder: SelectFragmentViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(userModels[position].profileImageUri)
                .apply { RequestOptions.circleCropTransform() }
                .into(holder.imageView)

            holder.textView.text = userModels[position].userName
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, MessageActivity::class.java)
                intent.putExtra("receiverUid", userModels[position].uid)
                val activityOptions = ActivityOptions.makeCustomAnimation(it.context, R.anim.appear_from_right, R.anim.disappear_to_right)
                startActivity(intent, activityOptions.toBundle())
            }
            holder.textView_statusMessage.text = userModels[position].statusMessage
            holder.checkBox.setOnCheckedChangeListener {
                    buttonView, isChecked ->
                val key = userModels[position].uid
                if (isChecked){
                    chatRoomModel.members[key] = true
                } else {
                    chatRoomModel.members.remove(key)
                }
            }
        }
    }
}
