package com.example.ever_after

import android.content.pm.PackageManager
import android.graphics.Color
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class BottomNavigation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveFCMToken()
        setContentView(R.layout.activity_bottom_navigation)

        // Set Toolbar as ActionBar
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }


        // Set up Navigation Controller
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        // Connect BottomNavigationView with Navigation Component
        bottomNavigationView.setupWithNavController(navController)

        // Enable Back Button Support in Toolbar
//        setupActionBarWithNavController(navController)  // Use only after setting the Toolbar
    }

    // Handle Up Button in Toolbar
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun saveFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                        .child("fcmToken").setValue(token)
                }
            }
        }
    }

}

