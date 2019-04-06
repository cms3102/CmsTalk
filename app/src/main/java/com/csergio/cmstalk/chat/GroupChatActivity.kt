package com.csergio.cmstalk.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.csergio.cmstalk.R
import com.csergio.cmstalk.model.ChatRoomModel
import com.csergio.cmstalk.model.NotificationModel
import com.csergio.cmstalk.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_group_chat.*
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_message.view.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : AppCompatActivity() {

    val usersMap = mutableMapOf<String, UserModel?>()
    var targetRoomId = ""
    var uid = ""

    private lateinit var databaseRef:DatabaseReference
    private lateinit var valueEventListener:ValueEventListener

    private val comments = mutableListOf<ChatRoomModel.Comment>()

    private var peopleCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        targetRoomId = intent.getStringExtra("targetRoomId")
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
//              usersMap = snapshot.value as MutableMap<String, UserModel>
                for (item in snapshot.children){
                    usersMap[item.key.toString()] = item.getValue(UserModel::class.java)
                }

                init()
            }

        })

    }

    fun init() {
        groupChatActivity_button.setOnClickListener {
            val comment = ChatRoomModel.Comment()
            comment.uid = uid
            comment.message = groupChatActivity_editText.text.toString()
            comment.timestamp = ServerValue.TIMESTAMP
            FirebaseDatabase.getInstance().getReference("chatRooms/$targetRoomId/comments").push().setValue(comment).addOnCompleteListener {
                FirebaseDatabase.getInstance().getReference("chatRooms/$targetRoomId/members").addListenerForSingleValueEvent(object :ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val map = snapshot.value as MutableMap<String, Boolean>
                        for (item in map.keys){
                            if (item == uid){
                                continue
                            }
                            sendGcm(usersMap[item]?.pushToken.toString())
                            groupChatActivity_editText.text?.clear()
                        }
                    }

                })
            }
        }
        groupChatActivity_recyclerView.adapter = GroupChatAdapter()
        groupChatActivity_recyclerView.layoutManager = LinearLayoutManager(this@GroupChatActivity)
    }

    inner class GroupChatViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val textView_message = itemView.item_message_textView
        val textView_name = itemView.item_message_textView_name
        val imageView_profile = itemView.item_message_imageView_profile
        val linearLayout_receiver = itemView.item_message_linearLayout_receiver
        val linearLayout_main = itemView.item_message_linearLayout_main
        val textView_timestamp = itemView.item_message_textView_timestamp
        val textView_readCountLeft = itemView.item_message_textView_readCount_left
        val textView_readCountRight = itemView.item_message_textView_readCount_right
    }

    // 채팅 내용 클라우드 메시징으로 보내기
    fun sendGcm(pushToken:String) {
        val gson = Gson()

        val userName = FirebaseAuth.getInstance().currentUser?.displayName
        val notificationModel = NotificationModel()
        notificationModel.to = pushToken
        notificationModel.notification.title = userName.toString()
        notificationModel.notification.text = groupChatActivity_editText.text.toString()
        notificationModel.data.title = userName.toString()
        notificationModel.data.text = groupChatActivity_editText.text.toString()

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel))
        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .addHeader("Authorization", "key=AIzaSyDu6TFX4tQIYJ5I1Key7pjoK4HTD8kG1zI")
            .url("https://gcm-http.googleapis.com/gcm/send")
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
            }

        })

    }

    inner class GroupChatAdapter:RecyclerView.Adapter<GroupChatViewHolder>{

        constructor(){
            getMessageList()
        }

        // 대화 내용 불러오기
        fun getMessageList() {
            databaseRef = FirebaseDatabase.getInstance().getReference("chatRooms/$targetRoomId").child("comments")
            valueEventListener = databaseRef.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    val readMembers = mutableMapOf<String, Any>()
                    for (item in snapshot.children){
                        val key = item.key.toString()
                        val commentOriginal = item.getValue(ChatRoomModel.Comment::class.java)
                        val commentModified = item.getValue(ChatRoomModel.Comment::class.java)
                        commentModified?.let {
                            commentModified.readMembers[uid] = true
                            readMembers[key] = commentModified
                        }
                        commentOriginal?.let {
                            comments.add(commentOriginal)
                        }
                    }

                    // 마지막 메시지의 읽은 사람 목록에 내가 없으면 서버로 읽었다고 업데이트
                    if (!comments[comments.size - 1].readMembers.containsKey(uid)){
                        FirebaseDatabase.getInstance().getReference("chatRooms").child(targetRoomId).child("comments")
                            .updateChildren(readMembers).addOnCompleteListener {
                                // 데이터 새로 고침
                                notifyDataSetChanged()
                                // 마지막 항목으로 스크롤 조정
                                groupChatActivity_recyclerView.scrollToPosition(comments.size - 1)
                            }
                    } else {
                        // 데이터 새로 고침
                        notifyDataSetChanged()
                        // 마지막 항목으로 스크롤 조정
                        groupChatActivity_recyclerView.scrollToPosition(comments.size - 1)
                    }

                }

            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return GroupChatViewHolder(view)
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: GroupChatViewHolder, position: Int) {
            // 내가 보낸 메시지
            if (comments[position].uid == uid){
                holder.textView_message.text = comments[position].message
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.linearLayout_receiver.visibility = View.INVISIBLE
                holder.linearLayout_main.gravity = Gravity.RIGHT
                setReadCount(position, holder.textView_readCountLeft)
                // 상대방이 보낸 메시지
            } else {
                Glide.with(holder.itemView.context)
                    .load(usersMap[comments[position].uid]?.profileImageUri)
                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.imageView_profile)
                holder.textView_name.text = usersMap[comments[position].uid]?.userName
                holder.linearLayout_receiver.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.textView_message.text = comments[position].message
                holder.textView_message.textSize = 25F
                holder.linearLayout_main.gravity = Gravity.LEFT
                setReadCount(position, holder.textView_readCountRight)
            }

            val unixTime = comments[position].timestamp as Long
            val simpleDataFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
            simpleDataFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val date = simpleDataFormat.format(Date(unixTime))
            holder.textView_timestamp.text = date
        }

        // 메시지 안 읽은 인원 수 표시
        private fun setReadCount(position: Int, textView: TextView) {
            if (peopleCount == 0){
                FirebaseDatabase.getInstance().getReference("chatRooms/$targetRoomId/members").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val usersSize = snapshot.childrenCount.toInt()
                        peopleCount = usersSize
                        val count = peopleCount - comments[position].readMembers.size
                        if (count > 0){
                            textView.visibility = View.VISIBLE
                            textView.text = count.toString()
                        } else {
                            textView.visibility = View.INVISIBLE
                        }
                    }

                })
            } else {
                val count = peopleCount - comments[position].readMembers.size
                if (count > 0){
                    textView.visibility = View.VISIBLE
                    textView.text = count.toString()
                } else {
                    textView.visibility = View.INVISIBLE
                }
            }
        }

    }

    override fun onBackPressed() {
        valueEventListener?.let {
            databaseRef.removeEventListener(it)
        }
        finish()
        // 뒤로 가기 버튼 눌렀을 때 애니메이션 세팅
        overridePendingTransition(R.anim.appear_from_left, R.anim.disappear_to_right)
    }
}
