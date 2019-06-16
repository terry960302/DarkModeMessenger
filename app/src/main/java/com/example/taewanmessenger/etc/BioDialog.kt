package com.example.taewanmessenger.etc

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.taewanmessenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.bio_dialog_layout.*

class BioDialog(context : Context): Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bio_dialog_layout)

        sendBio_button_bioDialog.setOnClickListener {

            val bio = bio_edittext_bioDialog.text
            if(TextUtils.isEmpty(bio)){
                Toast.makeText(context, "빈칸을 채워주세요.", Toast.LENGTH_SHORT).show()
            }
            else{
                sendBio_button_bioDialog.startAnimation()
                FirebaseFirestore.getInstance()
                    .collection("유저")
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .update("bio", bio.toString())
                    .addOnSuccessListener {
                        sendBio_button_bioDialog.revertAnimation()
                        dismiss()
                    }
            }

        }
    }
}