package com.example.ever_after

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

class MainActivity : AppCompatActivity() {
    private lateinit var video : VideoView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var share : SharePrefrence
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        share = SharePrefrence(this)
        video = findViewById(R.id.video)
        lottieAnimationView = findViewById(R.id.lotiieee)
        lottieAnimationView.playAnimation()

        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.loading}")
        video.setVideoURI(videoUri)
        video.start()

        video.setOnCompletionListener {
            if (share.checkLoginState()){
                val i = Intent(this,detailsPage::class.java)
                startActivity(i)
            } else{
                val i = Intent(this,LoginPage::class.java)
                startActivity(i)
            }
        }



    }
}