package com.example.ever_after

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        // Set Toolbar as ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        // Set up Navigation Controller
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        // Connect BottomNavigationView with Navigation Component
        bottomNavigationView.setupWithNavController(navController)

        // Enable Back Button Support in Toolbar
        setupActionBarWithNavController(navController)  // Use only after setting the Toolbar
    }

    // Handle Up Button in Toolbar
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

