package com.example.taewanmessenger

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.RequestOptions
import com.example.taewanmessenger.Utils.StorageUtil
import com.example.taewanmessenger.etc.BioDialog
import com.example.taewanmessenger.etc.GlideApp
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_my_page.*
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class MyPageActivity : AppCompatActivity() {

    private val RC_GALLERY = 1001
    private val auth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        /**
         * 툴바 설정
         * **/
        setSupportActionBar(toolbar_myPage)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "마이페이지"

        //프로필 이미지 가져오기
        val imagePath = intent.getStringExtra("profileImagePath")
        if(imagePath != null){
            GlideApp.with(this)
                .load(imagePath)
                .into(centerProfileImage_imageview_myPageActivity)
            GlideApp.with(this)
                .load(imagePath)
                .into(backgroundProfileImage_imaegview_myPageActivity)
        }
        profileName_textview_myPage.text = auth.currentUser?.displayName
        profileEmail_textview_myPage.text = auth.currentUser?.email
        FirebaseFirestore.getInstance()
            .collection("유저")
            .document(auth.uid.toString())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null) return@addSnapshotListener
                if(documentSnapshot != null){
                    if(documentSnapshot["bio"] != null){
                        bio_textview_myPage.text = documentSnapshot["bio"].toString()
                    }
                    else{
                        bio_textview_myPage.text = ""
                    }
                }
            }

        //로티애니메이션 가져오는 메서드
        getLottieAnim()

        //자기소개 변경
        bio_cardview_myPage.setOnClickListener {
            val dialog = BioDialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
        //프로필 사진 변경
        centerProfileImage_imageview_myPageActivity.setOnClickListener {
            val list = arrayOf("앨범에서 사진 선택", "기본 이미지")
            AlertDialog.Builder(this@MyPageActivity)
                .setItems(list) { dialog, which ->
                    when(list[which]){
                        "앨범에서 사진 선택" -> {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(intent, RC_GALLERY)
                        }
                        "기본 이미지" -> {
                            FirebaseFirestore.getInstance()
                                .collection("유저")
                                .document(FirebaseAuth.getInstance().uid.toString())
                                .update("profileImagePath", "https://firebasestorage.googleapis.com/v0/b/taewanmessenger.appspot.com/o/user6.png?alt=media&token=2f0c5f4f-85eb-489a-911d-4c94db9b51a7")
                        }
                    }
                }
                .show()
        }
    }

    private fun getLottieAnim() {
        lottie1.apply {
            this.setAnimation("onepiece.json")
            this.loop(true)
            this.playAnimation()
        }
        lottie2.apply {
            this.setAnimation("deadpool.json")
            this.loop(true)
            this.playAnimation()
        }
        lottie3.apply {
            this.setAnimation("car.json")
            this.loop(true)
            this.playAnimation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //갤러리에서만 사진 가져오기
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
            val progressDialog = indeterminateProgressDialog("프로필 사진 변경중")
            progressDialog.setCancelable(false)

            if(croppedUri != null){
                val bmp : Bitmap
                try {
                    bmp = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    val outputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val byteArray = outputStream.toByteArray()
                    StorageUtil.uploadProfileImage(byteArray, progressDialog)
                    GlideApp.with(this)
                        .load(bmp)
                        .into(centerProfileImage_imageview_myPageActivity)
                    GlideApp.with(this)
                        .load(bmp)
                        .into(backgroundProfileImage_imaegview_myPageActivity)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
