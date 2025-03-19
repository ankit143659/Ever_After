package com.example.ever_after

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2

class detailsPage : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var nextBtn: ImageButton
    private val totalPages = 13
    private lateinit var viewModel: dataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_page)

        viewModel = ViewModelProvider(this)[dataViewModel::class.java]

        viewPager2 = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)
        nextBtn = findViewById(R.id.btnNext)

        val adapter = ViewPagerAdapter(this)
        viewPager2.adapter = adapter

        viewPager2.isUserInputEnabled = false

        updateProgressBar(0)

        nextBtn.setOnClickListener {
            Log.d("CurrentPage", viewPager2.currentItem.toString())

            val currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager2.currentItem)
            if (currentFragment is detail_1) {
                if (!viewModel.isDataValid()) {
                    currentFragment.requireView().findViewById<TextView>(R.id.nameError).visibility = View.VISIBLE
                    currentFragment.requireView().findViewById<TextView>(R.id.dateError).visibility = View.VISIBLE
                    return@setOnClickListener
                }
            }

            if (viewPager2.currentItem == 12) {
                val intent = Intent(this, BottomNavigation::class.java)
                startActivity(intent)
            } else {
                val currentItem = viewPager2.currentItem
                if (currentItem < totalPages - 1) {
                    viewPager2.setCurrentItem(currentItem + 1, true)
                    updateProgressBar(currentItem + 1)
                }
            }
        }
    }

    private fun updateProgressBar(step: Int) {
        progressBar.progress = (step + 1) * (100 / totalPages)
    }
}
