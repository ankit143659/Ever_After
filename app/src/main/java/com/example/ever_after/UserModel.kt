package com.example.ever_after

data class UserModel(
    var userId: String = "",
    var matchPercentage: String = "",
    val name: String = "",
    val DOB: String = "",
    val Religion: String = "",
    val Interest: String = "", // String format "⚽ Music 🍕 Food 🎮 Gaming"
    var Image1: String ="" // Base64 encoded
)
