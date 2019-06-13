package com.example.taewanmessenger.Recyclerview

import android.content.Context
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import com.example.taewanmessenger.Models.ChatModel
import com.example.taewanmessenger.R
import com.example.taewanmessenger.etc.GlideApp
import com.example.taewanmessenger.etc.ZoomDialog
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.message_layout_left.view.*
import java.text.SimpleDateFormat
import java.util.*

class chatOtherToMe(val context : Context,
                    val chatModel: ChatModel) : Item<ViewHolder>() {

    lateinit var dialog : ZoomDialog

    override fun getLayout(): Int = R.layout.message_layout_left

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatLog_textview_left.text = chatModel.desc
        val format = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초")
        val date = format.format(Date(chatModel.time))
        viewHolder.itemView.chatTime_textview_left.text = date

        //이미지를 올렸을 경우에 없었던 이미지칸이 생기게 함.
        if(chatModel.imagePath != null && chatModel.desc == ""){
            viewHolder.itemView.chatLog_imageview_left.visibility = View.VISIBLE
            GlideApp.with(context)
                .load(chatModel.imagePath)
                .into(viewHolder.itemView.chatLog_imageview_left)
        }
        else{
            viewHolder.itemView.chatLog_imageview_left.visibility = View.GONE
        }


        viewHolder.itemView.setOnClickListener {
            //클릭했을 때 이미지일 경우만 반응
            if(chatModel.imagePath != null && chatModel.desc == ""){
                dialog = ZoomDialog(it.context, chatModel.imagePath)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.show()
            }
        }

        //애니메이션
        val animation = AnimationUtils.loadAnimation(context, R.anim.up_from_bottom)
        viewHolder.itemView.startAnimation(animation)
    }

}