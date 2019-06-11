package com.example.taewanmessenger

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.taewanmessenger.Utils.StorageUtil
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        profileImage_app_bar_mypageActivity.setOnClickListener {
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
                                .update("profileImagePath", null)
                        }
                    }
                }
                .show()
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
            val progressDialog = indeterminateProgressDialog("로딩중")
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
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
