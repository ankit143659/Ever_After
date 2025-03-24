package com.example.ever_after

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    var seen: Boolean = false
)