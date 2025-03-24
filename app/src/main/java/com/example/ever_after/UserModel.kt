package com.example.ever_after

data class UserModel(
    val userId: String = "",
    val name: String = "",
    val DOB: String = "",
    val Communities: String = "",
    val Interest: String = "", // String format "⚽ Music 🍕 Food 🎮 Gaming"
    var Image1: String ="" // Base64 encoded
)
