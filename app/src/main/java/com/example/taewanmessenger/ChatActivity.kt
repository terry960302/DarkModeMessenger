package com.example.taewanmessenger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taewanmessenger.Recyclerview.chatMeToOther
import com.example.taewanmessenger.Recyclerview.chatOtherToMe
import com.example.taewanmessenger.Models.ChatModel
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.example.taewanmessenger.Utils.StorageUtil
import com.example.taewanmessenger.etc.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.message_layout_right.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    private val TAG = "TAGChatActivity"
    lateinit var userInfo : UserModel
    private val RC_GALLERY = 1003
    lateinit var chatChannelId : String
    private val auth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        /**
         * MainActivity에서 데이터를 받음
         ***/
        val intent = this.intent
        userInfo = intent.extras.getSerializable("userInfo") as UserModel

        /**
         * 툴바 설정
         * **/
        setSupportActionBar(toolbar_chatActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = userInfo.name//채팅 상대방이름으로 등록
        //툴바에 상대방 이미지 등록
        GlideApp.with(this)
            .load(userInfo.profileImagePath)
            .into(myProfile_imageview_chatActivity)

        /**
         * 채팅방 생성(파이어스토어)
         * **/
//        //본인의 채팅방 경로
//        val myChannelRef = FirebaseFirestore.getInstance()
//            .collection("유저")
//            .document(auth.uid.toString())
//            .collection("채팅상대방")
//            .document(user.uid)
//        //상대방의 채팅방 경로
//        val yourChannelRef = FirebaseFirestore.getInstance()
//            .collection("유저")
//            .document(user.uid)
//            .collection("채팅상대방")
//            .document(auth.uid.toString())
//        myChannelRef.get().addOnSuccessListener {
//            if(it.exists()){
//                chatChannelId = it["channelId"].toString()
//            }
//
//            //채팅방을 이름으로 해서 컬렉션 만들어줌.
//            val channelRef = FirebaseFirestore.getInstance()
//                .collection("채팅방")
//                .document()
//
//            channelRef.set(mutableListOf(auth.uid.toString(), user.uid))
//
//            //채팅방 컬렉션에서 만든 도큐먼트 아이디를 유저 컬렉션 서브컬렉션에 저장
//            myChannelRef.set(mapOf("channelId" to channelRef.id))//내꺼에도 채팅방 아이디 만들어주고
//                .addOnSuccessListener {
//                    Log.d(TAG, "본인 유저 컬렉션에 채팅방 아이디를 등록했습니다.")
//                }
//
//            yourChannelRef.set(mapOf("channelId" to channelRef.id))//상대방꺼에 동시에 채팅방 아이디 만들어줌
//                .addOnSuccessListener {
//                    Log.d(TAG, "상대방 유저 컬렉션에 채팅방 아이디를 등록했습니다.")
//                }
//            // -> 이렇게 되면 하나의 채팅방을 둘이서 공유하는 방식이 됨.
//        }

        FirebaseFirestore.getInstance()
            .collection("유저")
            .document(auth.currentUser?.uid.toString())
            .collection("채팅상대방")
            .document(userInfo.uid)
            .get()
            .addOnSuccessListener {
                chatChannelId = it.get("channelId").toString()

                /**
                 * 리사이클러뷰 설정
                 * **/
                initRecyclerview(this, adapter, chatChannelId)

                /**
                 * 채팅보내기 설정
                 * **/
                //텍스트 메시지 보내기 버튼
                moreBtn_imageview_chatActivity.setOnClickListener {
                    if (TextUtils.isEmpty(chatInsert_edittext_chatActivity.text)) {
                        toast("메시지를 작성해주세요.")
                    } else {
                        FirestoreUtil.sendMessage(chatChannelId, chatInsert_edittext_chatActivity) {
                            //다 보내고 나면 텍스트를 싹 지워준다.
                            chatInsert_edittext_chatActivity.text.clear()
                        }
                    }
                }
                //이미지 메시지 보내기 버튼
                cameraBtn_imageview_chatActivity.setOnClickListener {
                    //여기서 갤러리 들어가는 과정
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, RC_GALLERY)
                }

            }

//        /**
//         * 리사이클러뷰 설정
//         * **/
//        initRecyclerview(this, adapter, chatChannelId)
//
//        /**
//         * 채팅보내기 설정
//         * **/
//        //텍스트 메시지 보내기 버튼
//        moreBtn_imageview_chatActivity.setOnClickListener {
//            if (TextUtils.isEmpty(chatInsert_edittext_chatActivity.text)) {
//                toast("메시지를 작성해주세요.")
//            } else {
//                FirestoreUtil.sendMessage(chatChannelId, chatInsert_edittext_chatActivity) {
//                    //다 보내고 나면 텍스트를 싹 지워준다.
//                    chatInsert_edittext_chatActivity.text.clear()
//                }
//            }
//        }
//        //이미지 메시지 보내기 버튼
//        cameraBtn_imageview_chatActivity.setOnClickListener {
//            //여기서 갤러리 들어가는 과정
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, RC_GALLERY)
//        }
    }
    /**
     * 여러 메서드들
     * **/
    fun initRecyclerview(context : Context,
                         adapter : GroupAdapter<ViewHolder>,
                         chatChannelId : String){
        //리사이클러뷰 기본 설정
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        chats_recyclerview_chatActivity.layoutManager = lm
        chats_recyclerview_chatActivity.adapter = adapter
//        if(chats_recyclerview_chatActivity.adapter!!.itemCount > 0){
//            chats_recyclerview_chatActivity.scrollToPosition(chats_recyclerview_chatActivity.adapter!!.itemCount -1)
//        }


        //기존에 카톡했던 내용을 시간순서대로 띄워줌
        FirebaseFirestore.getInstance()
            .collection("채팅방")
            .document(chatChannelId)
            .collection("채팅목록")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if(exception != null) return@addSnapshotListener
                if(snapshot != null){
                    for(dc in snapshot.documentChanges){
                        when(dc.type){
                            DocumentChange.Type.ADDED -> {
                                val id = dc.document["fromId"].toString()
                                val desc = dc.document["desc"].toString()
                                val imagePath = dc.document["imagePath"].toString()
                                val time = dc.document["time"] as Long

                                val chatLog = ChatModel(
                                    fromId = id,
                                    desc = desc,
                                    imagePath = imagePath,
                                    time = time)

                                //만약 내가 쓴글일 경우 오른쪽에 붙이고 상대방이 쓴 글이면 왼쪽에 붙임
                                if(id == FirebaseAuth.getInstance().uid.toString()){
                                    adapter.add(chatMeToOther(context, chatLog))
                                    adapter.notifyItemChanged(adapter.itemCount)//업데이트 된 내용만 애니메이션 작용
                                }
                                else{
                                    adapter.add(chatOtherToMe(context, chatLog))
                                    adapter.notifyItemChanged(adapter.itemCount)
                                }
                                chats_recyclerview_chatActivity.scrollToPosition(adapter.itemCount-1)
                            }
                        }
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_GALLERY && resultCode == Activity.RESULT_OK && data != null){
            val uri = data.data
            val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, "IMG_" + System.currentTimeMillis()))

            UCrop.of(uri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(450, 450)
                .start(this)
        }
        //크롭시작
        if(requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK){
            val croppedUri = UCrop.getOutput(data!!)
            val progressDialog = indeterminateProgressDialog("로딩중")
            if(croppedUri != null){
                val bmp : Bitmap
                try {
                    bmp = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    val outputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val byteArray = outputStream.toByteArray()
                    StorageUtil.uploadChatImage(byteArray, progressDialog)
                    GlideApp.with(this)
                        .load(bmp)
                        .into(chatLog_imageview_chatActivity)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
