package com.example.taewanmessenger.FCM

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {

    private val TAG = "TAGFCMIDservice"

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val refreshedToken = FirebaseInstanceId.getInstance().getToken()
        Log.d(TAG, "Refreshed Token : ${refreshedToken}")
    }
}