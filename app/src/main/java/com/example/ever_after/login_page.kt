package com.example.ever_after

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

class login_page : AppCompatActivity() {
    private lateinit var btnLogin : Button
    private lateinit var lottieAnimationView: LottieAnimationView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page2)
        btnLogin = findViewById(R.id.btnLogin)
        lottieAnimationView = findViewById(R.id.lotti)
        lottieAnimationView.playAnimation()
        btnLogin.setOnClickListener{
            val intent = Intent(this,detailsPage::class.java)
            startActivity(intent)
        }
    }
}