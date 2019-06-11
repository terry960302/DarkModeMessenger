package com.example.taewanmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taewanmessenger.Recyclerview.chatMeToOther
import com.example.taewanmessenger.Recyclerview.chatOtherToMe
import com.example.taewanmessenger.Models.ChatModel
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import java.util.*

class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(toolbar_chatActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "친구이름"

//        DummyData()

        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        chats_recyclerview_chatActivity.layoutManager = lm
        chats_recyclerview_chatActivity.adapter = adapter
        if(chats_recyclerview_chatActivity.adapter!!.itemCount > 0){
            chats_recyclerview_chatActivity.scrollToPosition(chats_recyclerview_chatActivity.adapter!!.itemCount -1)
        }

        /**
         * 채팅 연습용
         * **/


        moreBtn_imageview_chatActivity.setOnClickListener {
            if(TextUtils.isEmpty(chatInsert_edittext_chatActivity.text)){
                toast("메시지를 작성해주세요.")
            }
            else{

            }
        }
    }

//    private fun DummyData() {
//        val timeStamp = System.currentTimeMillis()
//        adapter.add(chatMeToOther(applicationContext, ChatModel("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//        adapter.add(chatMeToOther(applicationContext, ChatModel("내용입니다.", "", timeStamp)))
//
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//        adapter.add(chatOtherToMe(applicationContext, ChatModel("보내는 사람입니다.", "", timeStamp)))
//    }
}
