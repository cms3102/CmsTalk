package com.csergio.cmstalk.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.csergio.cmstalk.R
import com.csergio.cmstalk.chat.GroupChatActivity
import com.csergio.cmstalk.chat.MessageActivity
import com.csergio.cmstalk.model.ChatRoomModel
import com.csergio.cmstalk.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_chatroom.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment:Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        view.chatFragment_recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        view.chatFragment_recyclerView.adapter = ChatFragmentAdapter()

        return view
    }

    inner class ChatFragmentViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.chatItem_imageView
        val textView_title = itemView.chatItem_textView_title
        val textView_lastMessage = itemView.chatItem_textView_lastMessage
        val textView_timestamp = itemView.chatItem_textView_timestamp
    }

    inner class ChatFragmentAdapter: RecyclerView.Adapter<ChatFragmentViewHolder>{

        private var uid = ""
        private val chatRoomModels = mutableListOf<ChatRoomModel>()
        private val destinationUsers = mutableListOf<String>()
        private val keys = mutableListOf<String>()

        constructor(){
            uid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().getReference("chatRooms").orderByChild("members/$uid").equalTo(true).addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    chatRoomModels.clear()
                    for (item in snapshot.children){
                        val chatRoom =item.getValue(ChatRoomModel::class.java)
                        chatRoom?.let {
                            chatRoomModels.add(it)
                            keys.add(item.key.toString())
                        }
                        notifyDataSetChanged()
                    }
                }

            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatFragmentViewHolder {
            return ChatFragmentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chatroom, parent, false))
        }

        override fun getItemCount(): Int {
            return chatRoomModels.size
        }

        override fun onBindViewHolder(holder: ChatFragmentViewHolder, position: Int) {
            var receiverUid = ""
            for (memberId in chatRoomModels[position].members.keys){
                if (memberId != uid){
                    receiverUid = memberId
                    destinationUsers.add(receiverUid)
                }
            }

            // 일회용 리스너(ListenerForSingleValueEvent)로 값 가져오기
            FirebaseDatabase.getInstance().getReference("users").child(receiverUid).addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    Glide.with(holder.itemView.context)
                        .load(userModel?.profileImageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.imageView)
                    holder.textView_title.text = userModel?.userName
                }

            })

            val commentMap = TreeMap<String, ChatRoomModel.Comment>(Collections.reverseOrder())
            commentMap.putAll(chatRoomModels[position].comments)

            val keysArray = commentMap.keys.toTypedArray()
            if (keysArray.isNotEmpty()) {
                val lastMessageKey = keysArray.first()
                holder.textView_lastMessage.text = chatRoomModels[position].comments[lastMessageKey]?.message

                val simpleDateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
                simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                val unixTime = chatRoomModels[position].comments[lastMessageKey]?.timestamp as Long
                val date = simpleDateFormat.format(Date(unixTime))

                holder.textView_timestamp.text = date
            }

            holder.itemView.setOnClickListener {
                var intent:Intent
                if(chatRoomModels[position].members.size > 2){
                    intent = Intent(it.context, GroupChatActivity::class.java)
                    intent.putExtra("targetRoomId", keys[position])
                } else {
                    intent = Intent(it.context, MessageActivity::class.java)
                    intent.putExtra("receiverUid", destinationUsers[position])
                }
                val activityOptions = ActivityOptions.makeCustomAnimation(
                    it.context,
                    R.anim.appear_from_right,
                    R.anim.disappear_to_left
                ).toBundle()
                startActivity(intent, activityOptions)
            }

        }

    }

}

