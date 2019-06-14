package com.example.taewanmessenger.etc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taewanmessenger.R
import kotlinx.android.synthetic.main.activity_zoom.*

class ZoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom)

        val chatImagePath = intent.getStringExtra("chatImagePath")
        GlideApp.with(this)
            .load(chatImagePath)
            .into(zoomedImage_imageview_zoomActivity)
    }
}
