package com.example.taewanmessenger.etc

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.example.taewanmessenger.R
import kotlinx.android.synthetic.main.profile_dialog_layout.*

class Dialog_in_SearchItemClicked(context: Context,
                                  val profileImagePath : String?,
                                  val profileName : String,
                                  val profileEmail : String,
                                  val profileBio : String?,
                                  val btnListener : View.OnClickListener) : Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //다이얼로그 밖의 화면을 흐리게
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            this.flags =WindowManager.LayoutParams.FLAG_DIM_BEHIND
            this.dimAmount = 0.8f
        }
        window.attributes = layoutParams

        setContentView(R.layout.profile_dialog_layout)


        //리사이클러뷰 아이템에서 받은 걸 다이얼로그에 넣어줌
        //이미지 설정
        if(profileImagePath != null){
            GlideApp.with(context)
                .load(profileImagePath)
                .into(userProfile_imageview_searchActivity)
        } else{
            GlideApp.with(context)
                .load(R.drawable.users)
                .into(userProfile_imageview_searchActivity)
        }
        //이름 설정
        userName_textview_searchActivity.text = profileName
        //이메일 설정
        userEmail_textview_searchActivity.text = profileEmail
        //자기소개 설정
        userBio_textview_searchActivity.text = profileBio

        //친구 추가 누를시 이벤트가 FriendsItem_in_Search로 이동함.
        addFriend_button_searchActivity.setOnClickListener(btnListener)
    }
}