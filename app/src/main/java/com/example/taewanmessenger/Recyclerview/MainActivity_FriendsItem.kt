package com.example.taewanmessenger.Recyclerview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import com.example.taewanmessenger.ChatActivity
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.etc.GlideApp
import com.example.taewanmessenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main_friends_item.view.*
import java.util.*

class MainActivity_FriendsItem(val context : Context,
                               val user : UserModel) : Item<ViewHolder>() {

    private val TAG = "TAGMainActivity_Items"
    private val auth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    lateinit var myInfo : UserModel
    lateinit var chatChannelId : String

    override fun getLayout(): Int = R.layout.activity_main_friends_item

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.friendName_textview_friendsItem.text = user.name

        if(user.profileImagePath != null){
            GlideApp.with(context)
                .load(user.profileImagePath)
                .into(viewHolder.itemView.friendImage_imageview_friendsItem)
        }
        else{
            GlideApp.with(context)
                .load(R.drawable.users)
                .into(viewHolder.itemView.friendImage_imageview_friendsItem)
        }

        //아이템이 로드될 때마다 애니메이션 실행
        val animationFade = AlphaAnimation(0.0F, 1.0F)
        animationFade.duration = 500
        viewHolder.itemView.animation = animationFade

        /**
         * 친구를 골라서 들어가게 되면 채팅방 DB생성 -> 유저 정보를 다음 창인 ChatActivity로 보냄.
         * **/
        viewHolder.itemView.setOnClickListener {

            //채팅방 db생성
            chatChannelId = UUID.randomUUID().toString()//이렇게 하면 들어갈 때마다 방이 새로 생길듯
            FirebaseFirestore.getInstance()
                .collection("채팅방")
                .document(chatChannelId)
                .set(mapOf("channelId" to chatChannelId))
            Log.d(TAG, "채팅방(${chatChannelId}) 생성이 완료되었습니다.")

            //다음 창 이동 + 유저 데이터(내정보 + 친구 정보 + 채팅방 id) 보내기
            val intent = Intent(context, ChatActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("userInfo", user)//모델전체를 보내려면 모델클래스에서 Serializable하게 확장해줘야함.
            FirebaseFirestore.getInstance()
                .collection("유저")
                .document(auth.uid.toString())
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val result = it.result

                        val uid = result?.get("uid").toString()
                        val name = result?.get("name").toString()
                        val email = result?.get("email").toString()
                        val bio = result?.get("bio").toString()
                        val profileImagePath = result?.get("profileImagePath").toString()

                        myInfo = UserModel(uid, name, email, bio, profileImagePath)

                        bundle.putSerializable("myInfo", myInfo)
                        intent.putExtras(bundle)
                        intent.putExtra("chatChannelId", chatChannelId)
                        context.startActivity(intent)

                        Log.d(TAG, "MainActivity_FriendsItem -> ChatActivity로 이동합니다.")
                    }
                }
        }
    }
}