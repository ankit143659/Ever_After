package com.example.ever_after

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var video: VideoView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var share: SharePrefrence
    private lateinit var userRef: DatabaseReference

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
            checkUserData()
        }
    }

    private fun checkUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            navigateTo(LoginPage::class.java)
            return
        }

        userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Images")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.child("Image1").getValue(String::class.java)

                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    navigateTo(BottomNavigation::class.java)  // Image node hai, to direct BottomNavigation par jayega
                } else if (share.checkLoginState() && !share.checkDetailState()) {
                    navigateTo(detailsPage::class.java)  // Login kiya but details abhi nahi bhari to detailsPage par jayega
                } else {
                    navigateTo(LoginPage::class.java)  // Agar login nahi kiya to LoginPage par jayega
                }
            }

            override fun onCancelled(error: DatabaseError) {
                navigateTo(LoginPage::class.java)
            }
        })
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
        finish()
    }
}
