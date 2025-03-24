package com.example.ever_after

import java.io.Serializable

data class AppUser(
    val id: String,
    val name: String,
    var lastMessage: String = "",
    val profileImage: String = "",
    var timestamp: Long = 0,
    var isNewMessage: Boolean = false
) : Serializable