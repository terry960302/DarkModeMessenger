package com.example.taewanmessenger.Models

import java.io.Serializable

class UserModel(val uid : String,
                val name : String,
                val email : String,
                val bio : String?,
                val profileImagePath : String?):Serializable {
    constructor(): this("", "", "", null, null)
}