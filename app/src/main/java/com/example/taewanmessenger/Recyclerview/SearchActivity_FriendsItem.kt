package com.example.taewanmessenger.Recyclerview

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.R
import com.example.taewanmessenger.SearchActivity
import com.example.taewanmessenger.etc.Dialog_in_SearchItemClicked
import com.example.taewanmessenger.etc.GlideApp
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main_friends_item_in_search.view.*
import java.util.*
import javax.security.auth.callback.Callback

class SearchActivity_FriendsItem(val context: Context,
                                 val usermodel : UserModel) : Item<ViewHolder>(){

    private val TAG = "TAGSearchActivity_item"
    private lateinit var dialog : Dialog_in_SearchItemClicked

    override fun getLayout(): Int {
        return R.layout.activity_main_friends_item_in_search
    }


    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView
        //유저 이름 설정
        item.friendName_textview_friendsItem.text = usermodel.name
        //유저 이미지 설정
        val image = item.friendImage_imageview_friendsItem
        if(usermodel.profileImagePath != null || usermodel.profileImagePath != ""){
            GlideApp.with(context)
                .load(usermodel.profileImagePath)
                .into(image)
        }
        else {
            GlideApp.with(context)
                .load(R.drawable.users)
                .into(image)
        }


        //아이템이 로드될 때마다 애니메이션 실행
        val animationFade = AlphaAnimation(0.0F, 1.0F)
        animationFade.duration = 500
        viewHolder.itemView.animation = animationFade

        //아이템을 클릭하면 생기는 이벤트
        item.setOnClickListener {
            Toast.makeText(context, "안녕", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "검색한 목록의 이미지경로 = ${usermodel.profileImagePath.toString()}")
            callDialog(it.context)//https://stackoverflow.com/questions/28104663/unable-to-show-alertdialog-on-button-click-in-a-listview 이 사이트로 문제 해결.....후..
        }
    }
    fun callDialog(context : Context){
        dialog = Dialog_in_SearchItemClicked(
            context = context,
            profileImagePath = usermodel.profileImagePath,
            profileName = usermodel.name,
            profileEmail = usermodel.email,
            profileBio = usermodel.bio,
            btnListener = addFriendListener)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window.attributes.windowAnimations = R.anim.popup_for_dialog//다이얼로그 애니메이션
        dialog.show()
    }
    private val addFriendListener = View.OnClickListener{
        Toast.makeText(context, "'${usermodel.name}'님과 친구가 되었습니다.", Toast.LENGTH_SHORT).show()
        dialog.dismiss()

        FirebaseFirestore.getInstance().collection("유저")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection("친구목록")
            .document(usermodel.uid)
            .set(usermodel)
    }
}