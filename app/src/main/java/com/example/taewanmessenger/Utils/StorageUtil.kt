package com.example.taewanmessenger.Utils

import android.app.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object StorageUtil {
    private val firebaseAuthInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseStorageInstance : FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    fun uploadProfileImage(byteArray: ByteArray,
                           progressDialog: ProgressDialog
    ) {
        val profileRef = firebaseStorageInstance.reference.child("프로필사진/${UUID.nameUUIDFromBytes(byteArray)}")
        profileRef.putBytes(byteArray).addOnSuccessListener {
            profileRef.downloadUrl.addOnSuccessListener {
                //여기서 받은 파이어스토리지 내 uri를 이제 디비에 저장한 후 액티비티를 켤 때마다 프로필이미지를 불러온다.
                FirestoreUtil.profileImageToFirestore(it)
                progressDialog.dismiss()
            }
        }
    }
}