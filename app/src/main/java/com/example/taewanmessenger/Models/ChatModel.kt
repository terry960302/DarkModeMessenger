package com.example.taewanmessenger.Models

import java.sql.Timestamp

class ChatModel(val fromId : String,
                val desc : String?,
                val imagePath : String?,
                val time : Long) {
}