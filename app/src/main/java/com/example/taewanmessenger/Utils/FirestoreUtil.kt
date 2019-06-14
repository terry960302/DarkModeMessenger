package com.example.taewanmessenger.Utils

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.taewanmessenger.Models.ChatModel
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Recyclerview.MainActivity_FriendsItem
import com.example.taewanmessenger.Recyclerview.SearchActivity_FriendsItem
import com.example.taewanmessenger.Recyclerview.chatMeToOther
import com.example.taewanmessenger.Recyclerview.chatOtherToMe
import com.example.taewanmessenger.etc.GlideApp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView

object FirestoreUtil {

    private val TAG = "TAGFirestore"
    private val auth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val userDocRef : DocumentReference
        get() = firestoreInstance.collection("유저").document(FirebaseAuth.getInstance().uid.toString())

    /**
     * LoginActivity
     * **/
    //이메일로 가입한 유저 업로드
    fun firstEmailLoginUser(onComplete:()->Unit){
        userDocRef.get().addOnSuccessListener {
            if(!it.exists()){
                val newUser = UserModel(
                    uid = auth.uid.toString(),
                    name = auth.currentUser?.displayName?: "",//null값일 경우 ""로 처리
                    email = auth.currentUser?.email?: "",
                    bio = null,
                    profileImagePath = null
                )
                userDocRef.set(newUser).addOnCompleteListener {
                    Log.d(TAG, "파이어스토어에 이메일로 가입한 유저 정보가 업로드 완료했습니다.")
                    onComplete()
                }
            }
            else{
                Log.d(TAG, "파이어스토어에 이메일로 가입한 유저 정보가 업로드 실패")
                onComplete()
            }
        }
    }
    //구글로 가입한 유저 업로드
    fun firstGoogleLoginUser(account : GoogleSignInAccount,
                             onComplete: () -> Unit){
        userDocRef.get().addOnSuccessListener {
            if(!it.exists()){
                val newUser = UserModel(
                    uid = auth.uid.toString(),
                    name = account?.displayName?:"",
                    email = account?.email?:"",
                    bio = null,
                    profileImagePath = null
                )
                userDocRef.set(newUser).addOnCompleteListener {
                    Log.d(TAG, "파이어스토어에 구글로 가입한 유저 정보가 업로드 완료했습니다.")
                    onComplete()
                }
            }
            else{
                Log.d(TAG, "파이어스토어에 구글로 가입한 유저 정보가 업로드 실패")
                onComplete()
            }
        }
    }
    //페이스북으로 가입한 유저
    fun firstFacebookLoginUser(user : FirebaseUser?, onComplete: () -> Unit){
        userDocRef.get().addOnSuccessListener {
            if(!it.exists()){
                if(user == null) Log.d(TAG, "파이어스토어 페이스북에서 받은 유저 정보가 없습니다.")
                val newUser = UserModel(
                    uid = user?.uid.toString(),
                    name = user?.displayName.toString(),
                    email = user?.email.toString(),
                    profileImagePath = user?.photoUrl.toString(),
                    bio = null
                )
                userDocRef.set(newUser).addOnCompleteListener {
                    Log.d(TAG, "파이어스토어 페이스북 DB업로드 성공")
                    onComplete()
                }
            }
            else{
                Log.d(TAG, "파이어스토어 페이스북 DB업로드 실패")
                onComplete()
            }
        }
    }
    /**
     * SearchActivity
     * **/
    //유저 아이디 검색시 실시간으로 파이어스토어에서 검색결과를 가져옴.
    fun autoCompleteSearch(context: Context, autoComplete_textview : AutoCompleteTextView){
        firestoreInstance
            .collection("유저")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    var items = mutableListOf<String>()
                    result?.forEach {
                        items.add(it["name"].toString())
                    }
                    val array_adapter = ArrayAdapter<String>(context, R.layout.simple_dropdown_item_1line, items)
                    autoComplete_textview.setAdapter(array_adapter)
                }
            }
    }
    //검색 버튼을 누를시 결과물을 불러와서 리사이클러뷰에 붙여줌.
    fun fetchSearchedUser(context : Context, adapter : GroupAdapter<ViewHolder>, searchedText : String, textview : TextView, onComplete :() -> Unit){
        firestoreInstance
            .collection("유저")
            //작성한 글자중에 아이디에 한 자라도 포함되면 띄워줌
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    result?.forEach {
                        //이름중에 포함될 경우
                        if(searchedText in it["name"].toString()){
                            val uid = it["uid"].toString()
                            val name = it["name"].toString()
                            val email = it["email"].toString()
                            val bio = it["bio"].toString()
                            val profileImagePath = it["profileImagePath"].toString()
                            val searchedUser = UserModel(
                                uid = uid,//여기서 내 uid집어넣어서 오류계속 걸렸음 ㅅ불탱
                                name = name,
                                email = email,
                                bio = bio,
                                profileImagePath = profileImagePath
                            )
                            adapter.add(SearchActivity_FriendsItem(context, searchedUser))
                            Log.d(TAG, "어댑터에 검색한 친구목록 띄우기 성공")
                            //검색한 목록이 나오니 경고문은 없애줌.
                            textview.visibility = View.GONE
                            onComplete()
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                //검색결과가 없을 경우
                if(adapter.getItemCount() == 0){
                    //검색결과가 없다는 글자를 보여줌.
                    textview.visibility = View.VISIBLE
                    onComplete()
                }
            }
    }
    /**
     * MyPageActivity
     * **/
    //마이페이지에서 이미지 업로드하는 메서드
    fun profileImageToFirestore(imageUri : Uri){
        userDocRef.update("profileImagePath", imageUri.toString()).addOnSuccessListener {
            Log.d(TAG, "사용자의 프로필 사진 변경이 디비에 적용되었습니다.")
        }
    }
    /**
     * MainActivity
     * **/
    //내 친구들 다 불러오기
    fun fetchMyFriends(context : Context,
                       adapter : GroupAdapter<ViewHolder>,
                       progressDialog : ProgressDialog){
        FirebaseFirestore.getInstance()
            .collection("유저")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection("친구목록")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null) return@addSnapshotListener
                if(querySnapshot != null){

                    adapter.clear()//여기서 친구목록 미리 깔끔히 지워주고(중복으로 아이템 붙는거 방지)

                    for(dc in querySnapshot.documents){

                        val friendUid = dc["uid"].toString()

                        FirebaseFirestore.getInstance()
                            .collection("유저")
                            .whereEqualTo("uid", friendUid)
                            .orderBy("name")
                            .addSnapshotListener { snapshot, exception ->
                                if(exception != null) return@addSnapshotListener
                                if(snapshot != null){
                                    snapshot.forEach {
                                        val friend = it.toObject(UserModel::class.java)
                                        adapter.add(MainActivity_FriendsItem(context, friend))
                                    }
                                }
                            }
                    }
                    adapter.notifyDataSetChanged()//친구 추가나 삭제가 있을 경우 새로고침해줌.

//                    for(dc in querySnapshot.documentChanges){
//                        when(dc.type){
//                            //친구가 추가되었을 경우
//                            DocumentChange.Type.ADDED ->{
//
//                                val otherUid = dc.document["uid"].toString()
//
//                                FirebaseFirestore.getInstance()
//                                    .collection("유저")
//                                    .whereEqualTo("uid", otherUid)
//                                    .orderBy("name")
//                                    .addSnapshotListener { snapshot, exception ->
//                                        if(exception != null) return@addSnapshotListener
//                                        if(snapshot != null){
//                                            snapshot.forEach {
//                                                val otherUser = it.toObject(UserModel::class.java)
//                                                adapter.add(MainActivity_FriendsItem(context, otherUser))
//                                                Log.d(TAG, "이메일순으로 친구등록을 완료했습니다.")
//                                                adapter.notifyItemChanged(adapter.itemCount)
//                                            }
//                                        }
//                                    }
//                            }
//                            //친구 삭제된 요소에 대해
//                            DocumentChange.Type.REMOVED -> {
//                                val removedUser = dc.document.toObject(UserModel::class.java)//삭제된 사람의 정보
//                                adapter.notifyDataSetChanged()
//                                adapter.notifyDataSetChanged()
//                                adapter.notifyDataSetChanged()
//                                adapter.notifyDataSetChanged()
//                                adapter.notifyDataSetChanged()
////                                val otherUid = dc.document["uid"].toString()
////                                FirebaseFirestore.getInstance()
////                                    .collection("유저")
////                                    .whereEqualTo("uid", otherUid)
////                                    .orderBy("name", Query.Direction.DESCENDING)
////                                    .addSnapshotListener { snapshot, exception ->
////                                        if(exception != null) return@addSnapshotListener
////                                        if(snapshot != null){
////                                            snapshot.forEach {
////                                                val otherUser = it.toObject(UserModel::class.java)
////                                                adapter.notifyItemRemoved(dc.newIndex)
////                                                Log.d(TAG, "${otherUid}친구 삭제를 완료했습니다.")
//////                                                adapter.notifyItemChanged(adapter.itemCount)
////                                            }
////                                        }
////                                    }
//
//                            }
//                        }
//                    }
                    progressDialog.dismiss()
                }
            }
    }
    //툴바 우측 상단 프로필 이미지 불러오기
    fun toolbarProfileImage(context : Context,
                            profileImage : CircleImageView,
                            onComplete: (String) -> Unit){
        Log.d(TAG, "FirestoreUtil -> toolbarProfileImage함수 실행")

        FirebaseFirestore.getInstance().collection("유저")
            .document(auth.uid.toString())
                //스냅샷이 속도가 훨씬 빠름
            .addSnapshotListener { snapshot, firestoreException ->
                if(firestoreException != null) return@addSnapshotListener
                if(snapshot != null){
                    val imagePath = snapshot["profileImagePath"].toString()
                    if(imagePath != null){
                        GlideApp.with(context)
                            .load(imagePath)
                            .into(profileImage)
                        onComplete(imagePath)
                    }
                }
            }

    }
    /**
     * ChatActivity
     * **/
    //채팅방 접근에 필요한 도큐먼트 아이디 가져오기
    fun getChannelId(userInfo : UserModel, onComplete: (String) -> Unit){
        firestoreInstance
            .collection("유저")
            .document(auth.currentUser?.uid.toString())
            .collection("채팅상대방")
            .document(userInfo.uid)
            .get()
            .addOnSuccessListener {
                val channelId = it.get("channelId").toString()
                onComplete(channelId)
            }
    }
    fun fetchAllMessages(context : Context,
                         chatChannelId : String,
                         adapter : GroupAdapter<ViewHolder>,
                         recyclerview : RecyclerView,
                         progressBar: ProgressBar){

        //기존에 카톡했던 내용을 시간순서대로 띄워줌
        FirebaseFirestore.getInstance()
            .collection("채팅방")
            .document(chatChannelId)
            .collection("채팅목록")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if(exception != null) return@addSnapshotListener
                if(snapshot != null){
                    for(dc in snapshot.documentChanges){//변한 것만 올림
                        when(dc.type){
                            DocumentChange.Type.ADDED -> {//변한 것중에서 추가된 것만 선별해서 올림.

                                val chatLog = dc.document.toObject(ChatModel::class.java)

                                //만약 내가 쓴글일 경우 오른쪽에 붙이고 상대방이 쓴 글이면 왼쪽에 붙임
                                if(chatLog.fromId == FirebaseAuth.getInstance().uid.toString()){
                                    adapter.add(chatMeToOther(context, chatLog))
                                    adapter.notifyItemChanged(adapter.itemCount)//업데이트 된 내용만 애니메이션 작용
                                }
                                else{
                                    adapter.add(chatOtherToMe(context, chatLog))
                                    adapter.notifyItemChanged(adapter.itemCount)
                                }
                                recyclerview.scrollToPosition(adapter.itemCount-1)
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                    //채팅한 적이 없으면 로딩바 없애줌.
                    if(adapter.itemCount == 0 ){
                        progressBar.visibility = View.GONE
                    }
                    //참고용
                    //forEach문은 호출할 때마다 이전꺼까지 불러옴.(결국 1이라는 글 올리고 다음으로 2올리면 1,2 둘다 올라감.)
//                    snapshot.forEach {
//                        val chatLog = it.toObject(ChatModel::class.java)//객체로 바꾸려면 모델에 생성자가 있어야함.
//                        if(chatLog.fromId == FirebaseAuth.getInstance().uid.toString()){
//                            adapter.add(chatMeToOther(context, chatLog))
//                            adapter.notifyItemChanged(adapter.itemCount)
//                        }
//                        else{
//                            adapter.add(chatOtherToMe(context, chatLog))
//                            adapter.notifyItemChanged(adapter.itemCount)
//                        }
//                        chats_recyclerview_chatActivity.scrollToPosition(adapter.itemCount-1)
//                    }
                }
            }
    }
    //텍스트메시지를 파이어스토어에 저장(불러오기 X)
    fun sendTextMessage(chatChannelId : String, edittext : EditText, onComplete: () -> Unit){
        //메시지 보내질 경우 채팅방에 내가 쓴 글이 쌓임
        FirebaseFirestore.getInstance()
            .collection("채팅방")
            .document(chatChannelId)//null값으로 나오는 에러가 있음
            .collection("채팅목록")
            .document()//도큐먼트 아이디는 사용할 이유가 없으니 랜덤으로
            .set(
                ChatModel(
                fromId = auth.uid.toString(),//보낸 사람의 id필요
                desc = edittext.text.toString(),
                imagePath = null,//텍스트를 보내는 경우엔 이미지는 null
                time = System.currentTimeMillis()//시간순으로 나중에 정렬해야하므로
            ))
        onComplete()
    }
}