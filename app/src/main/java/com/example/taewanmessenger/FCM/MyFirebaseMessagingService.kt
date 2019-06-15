package com.example.taewanmessenger.FCM

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.taewanmessenger.ChatActivity
import com.example.taewanmessenger.MainActivity
import com.example.taewanmessenger.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "TAGFCMservice"

    //토큰을 받음.
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
    }

    //메시지 수신
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {//메시지 전체를 파라미터로 받음.

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage?.from}")//발신자 번호

        // Check if message contains a data payload.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)//이건 뭔지 모르겠음

            if (true) {//예약메시지의 경우
                scheduleJob()
            } else {//바로 보내는 메시지의 경우
                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage?.notification?.let {
            Log.d(TAG, "Message Notification title: ${it.title}")//제목이 들어감.
            Log.d(TAG, "Message Notification Body: ${it.body}")//내용이 들어감.

            sendNotification(it.title, it.body)
        }
    }
    private fun scheduleJob() {
        Log.d(TAG, "예약 메시지입니다.")
    }
    private fun handleNow(){
        Log.d(TAG, "즉시 보내는 메시지입니다.")
    }
    private fun sendNotification(title: String?, body: String?) {
//        if(title == null){
//            title = "푸쉬알림"
//        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.messenger_logo)//필수조건임.
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setLights(Color.BLUE, 1, 1)
            .setContentIntent(pendingIntent)
        val notificationManager
                = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
//    override fun onMessageReceived(remoteMessage : RemoteMessage?) {
//        super.onMessageReceived(remoteMessage)
//
//        if(remoteMessage?.notification != null){
//            val title = remoteMessage.notification!!.title
//            val body = remoteMessage.notification!!.body
//            Log.d(TAG, "Notification Title : ${title}")
//            Log.d(TAG, "Notification Body : ${body}")
//
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            val notificationBuilder = NotificationCompat.Builder(this)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setLights(Color.BLUE, 1, 1)
//                .setContentIntent(pendingIntent)
//            val notificationManager
//                    = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.notify(0, notificationBuilder.build())
//        }
//    }
}
