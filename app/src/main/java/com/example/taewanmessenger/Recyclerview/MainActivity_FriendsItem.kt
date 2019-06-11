package com.example.taewanmessenger.Recyclerview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import com.example.taewanmessenger.ChatActivity
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.etc.GlideApp
import com.example.taewanmessenger.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main_friends_item.view.*

class MainActivity_FriendsItem(val context : Context,
                               val user : UserModel) : Item<ViewHolder>() {

    lateinit var list : ArrayList<UserModel>

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

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            val bundle = Bundle()
            context.startActivity(intent)
        }

        //아이템이 로드될 때마다 애니메이션 실행
        val animationFade = AlphaAnimation(0.0F, 1.0F)
        animationFade.duration = 500
        viewHolder.itemView.animation = animationFade
    }
}