package com.example.taewanmessenger.Utils

import android.R
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Recyclerview.SearchActivity_FriendsItem
import com.example.taewanmessenger.etc.GlideApp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
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
    fun firstGoogleLoginUser(account : GoogleSignInAccount, onComplete: () -> Unit){
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
                //무조건 검색한 값이 동일한 경우만 리사이클러뷰에 띄워줌
//            .whereEqualTo("name", searchedText)
//            .get()
//            .addOnCompleteListener {
//                if(it.isSuccessful){
//                    Log.d(TAG, "onQueryTextChange -> addOnCompleteListener")
//                    val result = it.result
//                    result?.forEach {
//                        val name = it["name"].toString()
//                        val email = it["email"].toString()
//                        val bio = it["bio"].toString()
//                        val profileImagePath = it["profileImagePath"].toString()
//                        val searchedUser = UserModel(name = name, email = email,bio = bio, profileImagePath = profileImagePath)
//                        adapter.add(MainActivity_FriendsItem(context, searchedUser))
//                        Log.d(TAG, "어댑터에 검색한 친구목록 띄우기 성공")
//                        //검색한 목록이 나오니 경고문은 없애줌.
//                        textview.visibility = View.GONE
//                        onComplete()
//                    }
//                }
//                //검색결과가 없을 경우
//                if(adapter.getItemCount() == 0){
//                    //검색결과가 없다는 글자를 보여줌.
//                    textview.visibility = View.VISIBLE
//                    onComplete()
//                }
//            }
                //작성한 글자중에 아이디에 한 자라도 포함되면 띄워줌
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    result?.forEach {
                        //이름중에 포함될 경우
                        if(searchedText in it["name"].toString()){
                            val name = it["name"].toString()
                            val email = it["email"].toString()
                            val bio = it["bio"].toString()
                            val profileImagePath = it["profileImagePath"].toString()
                            val searchedUser = UserModel(
                                uid = auth.uid.toString(),
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
    fun toolbarProfileImage(context : Context, profileImage : CircleImageView){
        Log.d(TAG, "FirestoreUtil -> toolbarProfileImage함수 실행")

        FirebaseFirestore.getInstance().collection("유저")
            .document(auth.uid.toString())
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    val image = result?.get("profileImagePath")
                    if(image != null){
                        GlideApp.with(context)
                            .load(image)
                            .into(profileImage)
                    }

                }
            }
    }
    /**
     * ChatActivity
     * **/
    fun chatUploadImagePath(imageUri : Uri){
        FirebaseFirestore.getInstance()
            .collection("채팅방")
            .document("")
            .collection(FirebaseAuth.getInstance().uid.toString())
            .document("")
            .update("imagePath", imageUri.toString())
            .addOnSuccessListener {
                Log.d(TAG, "채팅방에 사진 업로드를 완료했습니다.")
            }
    }
}