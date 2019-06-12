package com.example.taewanmessenger.etc

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.example.taewanmessenger.R
import kotlinx.android.synthetic.main.zoom_dialog_layout.*

class ZoomDialog(context: Context,
                 val imagePath : String) : Dialog(context) {

    private val TAG = "TAGZoomDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //다이얼로그 밖의 화면을 흐리게
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            this.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            this.dimAmount = 0.8f
        }
        window.attributes = layoutParams

        setContentView(R.layout.zoom_dialog_layout)
        Log.d(TAG, "${imagePath}를 받았습니다.")
        GlideApp.with(context)
            .load(imagePath)
            .into(zoom_imageview_dialog)
    }
}