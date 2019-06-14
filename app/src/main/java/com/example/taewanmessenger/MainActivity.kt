package com.example.taewanmessenger

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Fade
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
    lateinit var profileImagePath : String

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
        FirestoreUtil.toolbarProfileImage(this, myProfile_imageview_mainActivity){
            profileImagePath = it
        }

        //툴바 우측 프로필 이미지 누르면 이동
        myProfile_imageview_mainActivity.setOnClickListener {

            val intent = Intent(this@MainActivity, MyPageActivity::class.java)
            //shared element transition화면 간 이동시 공유된 요소에서 일어나는 애니메이션 설정
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(
                    this@MainActivity,
                    myProfile_imageview_mainActivity,
                    ViewCompat.getTransitionName(myProfile_imageview_mainActivity)!!)
            intent.putExtra("profileImagePath", profileImagePath)
            startActivity(intent, options.toBundle())

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
        progressDialog = indeterminateProgressDialog("친구를 불러오는 중...")
        progressDialog.setCancelable(false)
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
        floatingActionMenu.close(false)
    }
}
