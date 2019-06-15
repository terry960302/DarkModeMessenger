package com.example.taewanmessenger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.taewanmessenger.FCM.AsyncTask_for_FCM
import com.example.taewanmessenger.Recyclerview.chatMeToOther
import com.example.taewanmessenger.Recyclerview.chatOtherToMe
import com.example.taewanmessenger.Models.ChatModel
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.example.taewanmessenger.Utils.FirestoreUtil.getChannelId
import com.example.taewanmessenger.etc.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.logging.type.HttpRequest
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
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
        //채팅방 아이디 가져오기
        FirestoreUtil.verifyChannelId(userInfo){
            chatChannelId = it

            /**
             * 리사이클러뷰 설정(채팅 목록 불러오기)
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
                        //FCM을 보내준다.
                        sendFCM_Message(userInfo, chatInsert_edittext_chatActivity.text.toString())
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

    private fun sendFCM_Message(usermodel : UserModel, message : String) {
        //클릭한 상대방의 FCM토큰을 가져온다.(FCM토큰은 기계별로 받는 고유한 값)(하나의 기계면 계정이 여러개라도 토큰은 동일, 대신 앱 지웠다 다시 깔면 토큰도 새로줌.)
        //그 토큰 주소로 메시지 내용을 쏴준다.
        FirebaseFirestore.getInstance()
            .collection("유저")
            .document(usermodel.uid)
            .addSnapshotListener { snapshot, exception ->
                if(exception != null) return@addSnapshotListener
                if(snapshot != null){
                    //상대방의 FCM토큰이 들어있는 데이터를 가져옴.
                    val user = snapshot.toObject(UserModel::class.java)
                    val userToken = user?.FCMtoken

                    //상대방 FCM토큰 주소로 보내기
                    Thread().run {
                        var aysnTask = AsyncTask_for_FCM()
                        aysnTask.doInBackground(userToken.toString(), message)
//                        try {
//                            val root = JSONObject()
//                            val notification = JSONObject()
//
//                            //FCM메시지 양식 생성
//                            notification.put("title", FirebaseAuth.getInstance().currentUser?.displayName.toString())
//                            notification.put("body", message)
//
//                            root.put("notification", notification)
//                            root.put("to", user?.FCMtoken)
//
//                            Log.d(TAG, notification.toString())
//                            Log.d(TAG, root.toString())//Json파일로 생성은 됨.
//
//                            //보내는 설정
//                            val Url = URL(FCM_MESSAGE_URL)
//                            val connection = Url.openConnection() as HttpURLConnection
//                            connection.requestMethod = "POST"
//                            connection.doOutput = true
//                            connection.doInput = true
//                            connection.addRequestProperty("Authorization", "key=${SERVER_KEY}")
//                            connection.addRequestProperty("Accept", "application/json")
//                            connection.addRequestProperty("Content-type", "application/json")
//
//                            val outputStream = connection.outputStream
//                            outputStream.write(root.toString().toByteArray(Charsets.UTF_8));
//                            connection.responseCode
//                            Log.d(TAG, "FCM메시지를 성공적으로 보냈습니다.")
////                            val retrofit = Retrofit.Builder()
////                                .baseUrl(FCM_MESSAGE_URL)
////                                .addConverterFactory(GsonConverterFactory.create())
////                                .build()
//
//                        }catch (e : Exception){
//                            e.printStackTrace()
//                            Log.d(TAG, "FCM메시지 보내기에 실패했습니다. -> ${e.message}")
//                        }
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
                                    sendFCM_Message(userInfo, "")
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
