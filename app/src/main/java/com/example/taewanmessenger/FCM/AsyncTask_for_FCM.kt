package com.example.taewanmessenger.FCM

import android.os.AsyncTask
import android.util.Log
import com.example.taewanmessenger.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AsyncTask_for_FCM : AsyncTask<String, Void, String>() {

    //엔드포인트
    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    //파이어베이스 '프로젝트 설정->클라우드 메시징' 에 있는 서버키
    private val SERVER_KEY = "AAAAKrhs_Dg:APA91bHWnqOc-BQvqcxIPvMyqzabfNTZTrRJ-24hEioRdwW31IGtK1vMyQEPSLf99d2Pw36mbxmHKaqNJPw0pP5omNy1sxjaNukOv5XRBuSHL-GwcVG9NS_04STA4VUOR3Pc46HefF4N"

    public override fun doInBackground(vararg params: String?): String {//파라미터로 String만 여러개 받음.
        val root = JSONObject()
        val notification = JSONObject()

        val userToken = params[0]//여러개 받은걸 인덱싱으로 매칭시켜줌.
        val message = params[1]

        //FCM메시지 양식 생성
        notification.put("title", FirebaseAuth.getInstance().currentUser?.displayName.toString())
        if(message == "") notification.put("body", "사진")

        root.put("notification", notification)
        root.put("to", userToken)

        Log.d("TAG", notification.toString())
        Log.d("TAG", root.toString())//Json파일로 생성은 됨.

        //보내는 설정
        val Url = URL(FCM_MESSAGE_URL)
        val connection = Url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true
        connection.addRequestProperty("Authorization", "key=${SERVER_KEY}")
        connection.addRequestProperty("Accept", "application/json")
        connection.addRequestProperty("Content-type", "application/json")
        connection.connect()

        val outputStream = connection.outputStream
        outputStream.write(root.toString().toByteArray(Charsets.UTF_8));
        connection.responseCode
        Log.d("TAG", "FCM메시지를 성공적으로 보냈습니다.")

        return "성공"
    }
}