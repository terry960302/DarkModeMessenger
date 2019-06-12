package com.example.taewanmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taewanmessenger.Recyclerview.MainActivity_FriendsItem
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class MainActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 툴바설정
         * **/
        //툴바 제목 설정
        setSupportActionBar(toolbar_mainActivity)
        supportActionBar?.title = "채팅앱"

        //툴바 내 프로필 사진띄우기
        FirestoreUtil.toolbarProfileImage(this, myProfile_imageview_mainActivity)

        //툴바 프로필 이미지 누르면 이동
        myProfile_imageview_mainActivity.setOnClickListener {
            startActivity(intentFor<MyPageActivity>().newTask())
        }


        /**
         * Fab버튼 설정
         * **/
        //검색 버튼 누를 때 이동
        search_fab_mainActivity.setOnClickListener {
            startActivity(intentFor<SearchActivity>().newTask())
        }

        //로그아웃 버튼
        logOut_fab_mainActivity.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(intentFor<LoginActivity>().newTask().clearTask())
        }


        /**
         * 리사이클러뷰 설정
         * **/
        initRecyclerview()
    }

    fun initRecyclerview(){
    //리사이클러뷰에 내 친구들 띄우기
    FirebaseFirestore.getInstance()
        .collection("유저")
        .document(FirebaseAuth.getInstance().uid.toString())
        .collection("친구목록")
        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null) return@addSnapshotListener
            if(querySnapshot != null){
                for(dc in querySnapshot.documentChanges){
                    when(dc.type){
                        DocumentChange.Type.ADDED ->{
                            val model = dc.document

                            val otherUid = model["uid"].toString()
                            val name = model["name"].toString()
                            val email = model["email"].toString()
                            val bio = model["bio"].toString()
                            val imagePath = model["profileImagePath"].toString()

                            val friend = UserModel(otherUid, name, email, bio, imagePath)
                            adapter.add(MainActivity_FriendsItem(this, friend))
                        }
                    }
                }
                adapter.notifyItemChanged(adapter.itemCount)
            }
        }

    //리사이클러뷰 설정
    friendsList_recyclerview_mainActivity.layoutManager = GridLayoutManager(this, 2)
    friendsList_recyclerview_mainActivity.setHasFixedSize(true)
    friendsList_recyclerview_mainActivity.adapter = adapter
    }

    //앱 시작시 작동 메서드
    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().uid == null){
            startActivity(intentFor<LoginActivity>().newTask().clearTask())
        }
    }
}
