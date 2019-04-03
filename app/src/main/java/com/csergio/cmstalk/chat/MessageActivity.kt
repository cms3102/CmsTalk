package com.csergio.cmstalk.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.csergio.cmstalk.R
import com.csergio.cmstalk.model.ChatRoomModel
import com.csergio.cmstalk.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_message.view.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {

    private var uid = ""
    private var receiverUid = ""
    private var chatRoomId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        uid = FirebaseAuth.getInstance().currentUser!!.uid
        receiverUid = intent.getStringExtra("receiverUid")

        // 현재 들어온 방의 아이디를 찾아서 변수에 저장
        checkChatRoom()

        messageActivity_button.setOnClickListener {
            val chatRoomModel = ChatRoomModel()
            chatRoomModel.members[uid] = true
            chatRoomModel.members[receiverUid] = true

            // 메시지 전송 요청 후 처리 완료 전까지 전송 버튼 클릭 안 되게 처리
            messageActivity_button.isClickable = false

            if (chatRoomId == ""){
                FirebaseDatabase.getInstance().getReference("chatRooms").push().setValue(chatRoomModel).addOnSuccessListener {
                    checkChatRoom()
                }
            } else {
                saveCommentToDB()
            }

            // 메시지 전송 처리 후 다시 전송 버튼 클릭 가능하게 처리
            messageActivity_button.isClickable = true

        }
    }

    // 보낸 메시지 DB 저장
    private fun saveCommentToDB() {
        val message = messageActivity_editText.text.toString()
        // 메시지 내용이 있을 때만 서버로 전송
        if (message != ""){
            val comment = ChatRoomModel.Comment()
            comment.uid = uid
            comment.message = message
            comment.timestamp = ServerValue.TIMESTAMP
            messageActivity_editText.text?.clear()
            FirebaseDatabase.getInstance().getReference("chatRooms/$chatRoomId").child("comments").push().setValue(comment)
        }
    }

    // 선택한 상대와 대화하던 방이 있는지 확인
    private fun checkChatRoom(){
        // 내가 들어 있는 방들을 가져온다
        FirebaseDatabase.getInstance().getReference("chatRooms").orderByChild("members/$uid").equalTo(true).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children){
                    val chatModel = item.getValue(ChatRoomModel::class.java)
                    chatModel?.let {
                        // 내가 들어가 있는 방 중에서 지금 내가 선택한 사람이 들어있는 방을 찾아서 방 아이디를 저장
                        if (chatModel.members.containsKey(receiverUid)){
                            chatRoomId = item.key!!
                            saveCommentToDB()
                            messageActivity_recyclerView.layoutManager = LinearLayoutManager(this@MessageActivity)
                            messageActivity_recyclerView.adapter = MessageActivityAdapter()
                        }
                    }
                }
            }

        })
    }

    inner class MessageActivityViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val textView_message = itemView.item_message_textView
        val textView_name = itemView.item_message_textView_name
        val imageView_profile = itemView.item_message_imageView_profile
        val linearLayout_receiver = itemView.item_message_linearLayout_receiver
        val linearLayout_main = itemView.item_message_linearLayout_main
        val textView_timestamp = itemView.item_message_textView_timestamp

    }

    inner class MessageActivityAdapter:RecyclerView.Adapter<MessageActivityViewHolder>{

        private val comments = mutableListOf<ChatRoomModel.Comment>()
        private lateinit var userModel:UserModel

        constructor(){
            FirebaseDatabase.getInstance().getReference("users").child(receiverUid).addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserModel::class.java)?.let {
                        userModel = it
                        getMessageList()
                    }
                }

            })
        }

        // 대화 내용 불러오기
        fun getMessageList() {
            FirebaseDatabase.getInstance().getReference("chatRooms/$chatRoomId").child("comments").addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for (item in snapshot.children){
                        val comment = item.getValue(ChatRoomModel.Comment::class.java)
                        comment?.let {
                            comments.add(comment)
                        }
                    }
                    // 데이터 새로 고침
                    notifyDataSetChanged()
                    // 마지막 항목으로 스크롤 조정
                    messageActivity_recyclerView.scrollToPosition(comments.size - 1)
                }

            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageActivityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)

            return MessageActivityViewHolder(view)
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: MessageActivityViewHolder, position: Int) {
            // 내가 보낸 메시지
            if (comments[position].uid == uid){
                holder.textView_message.text = comments[position].message
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.linearLayout_receiver.visibility = View.INVISIBLE
                holder.linearLayout_main.gravity = Gravity.RIGHT
            // 상대방이 보낸 메시지
            } else {
                Glide.with(holder.itemView.context)
                    .load(userModel.profileImageUri)
                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.imageView_profile)
                holder.textView_name.text = userModel.userName
                holder.linearLayout_receiver.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.textView_message.text = comments[position].message
                holder.textView_message.textSize = 25F
                holder.linearLayout_main.gravity = Gravity.LEFT
            }

            val unixTime = comments[position].timestamp as Long
            val simpleDataFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
            simpleDataFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val date = simpleDataFormat.format(Date(unixTime))
            holder.textView_timestamp.text = date
        }

    }
}
