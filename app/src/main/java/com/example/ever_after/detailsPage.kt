package com.example.ever_after

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class detailsPage : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var nextBtn: ImageButton
    private val totalPages = 13
    private lateinit var viewModel: dataViewModel
    private lateinit var viewModel2: dataModel2
    private lateinit var share : SharePrefrence

    private lateinit var loadingDialog : Dialog

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_page)

        viewModel = ViewModelProvider(this)[dataViewModel::class.java]
        viewModel2 = ViewModelProvider(this)[dataModel2::class.java]

        share = SharePrefrence(this)

        viewPager2 = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)
        nextBtn = findViewById(R.id.btnNext)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Current User ka UID
            database = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Details")
        } else {
            database = FirebaseDatabase.getInstance().getReference("Users").child("Unknown")
        }

        setupLoadingDialog()
        val adapter = ViewPagerAdapter(this)
        viewPager2.adapter = adapter

        viewPager2.isUserInputEnabled = false

        updateProgressBar(0)

        nextBtn.setOnClickListener {
            loadingDialog.show()
            Handler().postDelayed({
                Log.d("CurrentPage", viewPager2.currentItem.toString())
                val currentFragment =
                    supportFragmentManager.findFragmentByTag("f" + viewPager2.currentItem)
                if (currentFragment is detail_1) {
                    if (!viewModel.isDataValid()) {
                        loadingDialog.dismiss()
                        currentFragment.requireView()
                            .findViewById<TextView>(R.id.nameError).visibility = View.VISIBLE
                        currentFragment.requireView()
                            .findViewById<TextView>(R.id.dateError).visibility = View.VISIBLE
                        return@postDelayed
                    }
                } else if (currentFragment is detail_2) {
                    if (!viewModel.gendervalueSelected()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select gender", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_3) {
                    if (!viewModel.purposeValue()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_4) {
                    if (!viewModel.genderMeetiing()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_5) {
                    viewModel.selectedOptions.observe(this) { selectedList ->
                        Log.d("List", "$selectedList")
                        database.child("Hope").setValue(selectedList)
                    }
                } else if (currentFragment is detail_6) {
                    if (!viewModel.checkHeight()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please select your Height", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_7) {
                    if (!viewModel.checkInterest()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_8) {
                    if (!viewModel.checkInterest()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_9) {
                    if (!viewModel.checkInterest() && !viewModel2.checkInterest()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_10) {
                    if (!viewModel.checkInterest() && !viewModel2.checkInterest()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_11) {
                    if (!viewModel.checkInterest() && !viewModel2.checkInterest()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_12) {
                    if (!viewModel.checkInterest() && !viewModel2.checkInterest()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                } else if (currentFragment is detail_12) {
                    if (!viewModel.checkImage()) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Please Select atleast one", Toast.LENGTH_SHORT).show()
                        return@postDelayed
                    }
                }

                if (viewPager2.currentItem == 12 && viewModel.checkImage()) {
                    share.DetailState(true)
                    loadingDialog.dismiss()
                    val intent = Intent(this, BottomNavigation::class.java)
                    startActivity(intent)
                } else {
                    loadingDialog.dismiss()
                    val currentItem = viewPager2.currentItem
                    if (currentItem < totalPages - 1) {
                        viewPager2.setCurrentItem(currentItem + 1, true)
                        updateProgressBar(currentItem + 1)
                    }
                }



                loadingDialog.dismiss()
            },1500)
        }
}


    private fun updateProgressBar(step: Int) {
        progressBar.progress = (step + 1) * (100 / totalPages)
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setContentView(R.layout.loading_dialog)  // Custom Lottie Layout
        loadingDialog.setCancelable(false)  // Disable outside touch
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent BG
    }
}
