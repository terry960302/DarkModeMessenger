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
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.taewanmessenger.Recyclerview.chatMeToOther
import com.example.taewanmessenger.Recyclerview.chatOtherToMe
import com.example.taewanmessenger.Models.ChatModel
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.example.taewanmessenger.etc.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chat.*
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
        //채널 아이디를 가져와서 그걸로 리사이클러뷰 설정과 메시지 보내기 설정함.
        FirestoreUtil.getChannelId(userInfo){
            chatChannelId = it
            Log.d(TAG, "유저 -> 채팅상대방 에서 가져온 채널 아이디 = (${chatChannelId})")

            /**
             * 리사이클러뷰 설정
             * **/
            initRecyclerview(adapter, chatChannelId, progress_bar_chatActivity)
            /**
             * 채팅보내기 설정
             * **/

            //텍스트 메시지 보내기 버튼
            sendBtn_imageview_chatActivity.setOnClickListener {
                if (TextUtils.isEmpty(chatInsert_edittext_chatActivity.text)) {
                    toast("메시지를 작성해주세요.")
                } else {
                    FirestoreUtil.sendTextMessage(chatChannelId, chatInsert_edittext_chatActivity) {
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
    }


    /**
     * 여러 메서드들
     * **/
    fun initRecyclerview(adapter : GroupAdapter<ViewHolder>,
                         chatChannelId : String,
                         progressbar : ProgressBar){
        //리사이클러뷰 기본 설정
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        chats_recyclerview_chatActivity.layoutManager = lm
        chats_recyclerview_chatActivity.adapter = adapter

        FirestoreUtil.fetchAllMessages(this,
            chatChannelId,
            adapter,
            chats_recyclerview_chatActivity,
            progressbar)
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
//            val progressDialog = indeterminateProgressDialog("로딩중")
            if(croppedUri != null){
                val bmp : Bitmap
                try {
                    bmp = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    val outputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val byteArray = outputStream.toByteArray()
                    Log.d(TAG, "크롭한 이미지를 용량 줄여서 변환 완료했습니다.")

                    val circularProgressDrawable = CircularProgressDrawable(this)
                    circularProgressDrawable.strokeWidth = 30f
                    circularProgressDrawable.centerRadius = 30f
                    circularProgressDrawable.backgroundColor = R.color.colorAccent
                    circularProgressDrawable.start()

                    //받은 크롭 uri를 가지고 채팅로그에 붙여줌.
                    val chatImageStorageRef = FirebaseStorage.getInstance()
                        .reference.child("${FirebaseAuth.getInstance().uid.toString()}/${UUID.nameUUIDFromBytes(byteArray)}")
                    chatImageStorageRef.putBytes(byteArray).addOnSuccessListener {
                        Log.d(TAG, "채팅 이미지를 스토리지에 업로드했습니다.")
                        chatImageStorageRef.downloadUrl.addOnSuccessListener {
                            FirebaseFirestore.getInstance()
                                .collection("채팅방")
                                .document(chatChannelId)
                                .collection("채팅목록")
                                .document()
                                .set(
                                    ChatModel(
                                        fromId = FirebaseAuth.getInstance().uid.toString(),
                                        desc = "",
                                        imagePath = it.toString(),
                                        time = System.currentTimeMillis()
                                    )
                                ).addOnSuccessListener {
                                    Log.d(TAG, "채팅 이미지를 파이어스토어에 업로드했습니다.")
                                }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
