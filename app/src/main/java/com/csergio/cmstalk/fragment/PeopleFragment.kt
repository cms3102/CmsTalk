package com.csergio.cmstalk.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.csergio.cmstalk.R
import com.csergio.cmstalk.chat.MessageActivity
import com.csergio.cmstalk.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_people.view.*
import kotlinx.android.synthetic.main.item_friend.view.*

class PeopleFragment:Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_people, container, false)

        view.peopleFragment_recyclerView.layoutManager = LinearLayoutManager(this.context)
        view.peopleFragment_recyclerView.adapter = PeopleFragmentAdapter()

        view.peopleFragment_floatingActionButton.setOnClickListener {
            startActivity(Intent(view.context, SelectFriendActivity::class.java))
        }

        return view
    }

    inner class PeopleFragmentAdapter: RecyclerView.Adapter<PeopleFragmentViewHolder>{

        val userModels = mutableListOf<UserModel>()

        constructor(){
            FirebaseDatabase.getInstance().getReference("users").addValueEventListener(object : ValueEventListener{

                val myUid = FirebaseAuth.getInstance().currentUser?.uid

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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleFragmentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
            return PeopleFragmentViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userModels.size
        }

        override fun onBindViewHolder(holder: PeopleFragmentViewHolder, position: Int) {
            val imageViewContext = holder.itemView.context
            Glide.with(imageViewContext)
                .load(userModels[position].profileImageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView)

            holder.textView.text = userModels[position].userName
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, MessageActivity::class.java)
                intent.putExtra("receiverUid", userModels[position].uid)
                val activityOptions = ActivityOptions.makeCustomAnimation(it.context, R.anim.appear_from_right, R.anim.disappear_to_right)
                startActivity(intent, activityOptions.toBundle())
            }
            holder.textView_statusMessage.text = userModels[position].statusMessage
        }
    }

    inner class PeopleFragmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.item_friend_imageView
        val textView = itemView.item_friend_textView
        val textView_statusMessage = itemView.item_friend_textView_comment
    }
}