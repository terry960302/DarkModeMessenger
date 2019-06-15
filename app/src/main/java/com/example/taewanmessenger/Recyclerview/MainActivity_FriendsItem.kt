package com.example.taewanmessenger.Recyclerview

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import com.example.taewanmessenger.ChatActivity
import com.example.taewanmessenger.MainActivity
import com.example.taewanmessenger.Models.UserModel
import com.example.taewanmessenger.etc.GlideApp
import com.example.taewanmessenger.R
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main_friends_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import java.util.*

class MainActivity_FriendsItem(val context : Context,
                               val user : UserModel) : Item<ViewHolder>() {

    private val TAG = "TAGMainActivity_Items"
    private val auth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    lateinit var myInfo : UserModel
    lateinit var chatChannelId : String

    override fun getLayout(): Int = R.layout.activity_main_friends_item

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.friendName_textview_friendsItem.text = user.name

        if(user.profileImagePath != null){
            GlideApp.with(context)
                .load(user.profileImagePath)
                .into(viewHolder.itemView.friendImage_imageview_friendsItem)
        }
        else{
            GlideApp.with(context)
                .load(R.drawable.users)
                .into(viewHolder.itemView.friendImage_imageview_friendsItem)
        }

        //아이템이 로드될 때마다 애니메이션 실행
        val animationFade = AlphaAnimation(0.0F, 1.0F)
        animationFade.duration = 500
        viewHolder.itemView.animation = animationFade

        /**
         * 친구골라서 아이템 클릭 -> 채팅방 DB생성 -> 유저 정보를 다음 창인 ChatActivity로 보냄.
         * **/
        //아이템 짧게 클릭
        viewHolder.itemView.setOnClickListener {
            //다음 창 이동 + 유저 데이터(내정보 + 친구 정보 + 채팅방 id) 보내기
            val intent = Intent(context, ChatActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("userInfo", user)//모델전체를 보내려면 모델클래스에서 Serializable하게 확장해줘야함.
            intent.putExtras(bundle)
            context.startActivity(intent)
            Log.d(TAG, "MainActivity_FriendsItem -> ChatActivity로 이동합니다.")
        }

        viewHolder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setMessage("친구를 삭제하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("삭제", DialogInterface.OnClickListener { dialog, which ->
                    FirebaseFirestore.getInstance()
                        .collection("유저")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .collection("친구목록")
                        .document(user.uid)
                        .delete().addOnSuccessListener {
                            Log.d(TAG, "${user.name}님이 친구목록에서 삭제되었습니다.")
                        }
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })
                .show()
            return@setOnLongClickListener true
        }
    }
}