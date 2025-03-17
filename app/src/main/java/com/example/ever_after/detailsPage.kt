package com.example.ever_after

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class detailsPage : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var nextBtn : ImageButton
    private val total_pages = 12
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_page)

        viewPager2 = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)
        nextBtn = findViewById(R.id.btnNext)

        val adapter = ViewPagerAdapter(this)
        viewPager2.adapter = adapter

        viewPager2.isUserInputEnabled = false

        updateProgressBar(0)

        nextBtn.setOnClickListener{
            val currentItem = viewPager2.currentItem
            if (currentItem<total_pages-1){
                viewPager2.setCurrentItem(currentItem+1,true)
                updateProgressBar(currentItem+1)
            }
        }



    }

    private fun updateProgressBar(step: Int) {
        progressBar.progress = (step + 1) * (100 / total_pages)
    }
}