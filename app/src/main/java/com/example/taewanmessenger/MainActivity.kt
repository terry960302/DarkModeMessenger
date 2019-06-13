package com.example.taewanmessenger

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taewanmessenger.Recyclerview.MainActivity_FriendsItem
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class MainActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val TAG = "TAGMainActivity"
    lateinit var progressDialog : ProgressDialog

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
        progressDialog = indeterminateProgressDialog("친구를 불러오고 있습니다...")
        FirestoreUtil.fetchMyFriends(this, adapter, progressDialog)

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

    override fun onPause() {
        super.onPause()
        progressDialog.dismiss()
    }
}
