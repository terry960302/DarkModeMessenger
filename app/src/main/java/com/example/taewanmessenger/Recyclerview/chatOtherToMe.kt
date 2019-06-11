package com.example.taewanmessenger.Recyclerview

import android.content.Context
import android.view.animation.AnimationUtils
import com.example.taewanmessenger.Models.ChatModel
import com.example.taewanmessenger.R
import com.example.taewanmessenger.etc.GlideApp
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.message_layout_left.view.*
import java.text.SimpleDateFormat
import java.util.*

class chatOtherToMe(val context : Context,
                    val chatModel: ChatModel) : Item<ViewHolder>() {
    override fun getLayout(): Int = R.layout.message_layout_left

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatLog_textview_left.text = chatModel.desc
        val format = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초")
        val date = format.format(Date(chatModel.time))
        viewHolder.itemView.chatTime_textview_left.text = date
        GlideApp.with(context)
            .load(chatModel.imagePath)
            .into(viewHolder.itemView.chatLog_imageview_left)

        //애니메이션
        val animation = AnimationUtils.loadAnimation(context, R.anim.up_from_bottom)
        viewHolder.itemView.startAnimation(animation)
    }

}