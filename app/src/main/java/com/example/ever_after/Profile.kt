package com.example.ever_after

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayInputStream

class Profile : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var sharedPref: SharePrefrence
    private lateinit var btnLogOut: MaterialButton
    private lateinit var profile_image: ShapeableImageView
    // Use the actual userId dynamically

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Database Reference
        val auth = FirebaseAuth.getInstance().currentUser
        val userId = auth?.uid
        database = userId?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }!!

        // Initialize Views
        sharedPref = SharePrefrence(requireContext())
        usernameTextView = view.findViewById(R.id.username)
        emailTextView = view.findViewById(R.id.bio) // Assuming bio TextView is for email
        phoneTextView = view.findViewById(R.id.friends_count) // Assuming friends_count is for phone
        gridLayout = view.findViewById(R.id.gridLayout)
        btnLogOut = view.findViewById(R.id.btnLogOut)
        profile_image = view.findViewById(R.id.profile_image)

        fetchUserData()
        btnLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPref.logoutUser()
            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), login_page::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }

    private fun fetchUserData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)

                    usernameTextView.text = name
                    emailTextView.text = email
                    phoneTextView.text = phone

                    val imagesSnapshot = snapshot.child("Images")
                    val image1=imagesSnapshot.child("Image1").getValue(String::class.java)
                    profile_image.setImageBitmap(image1?.let { decodeBase64ToBitmap(it) })

                    val imageUrls = mutableListOf<String>()

                    for (image in imagesSnapshot.children) {
                        val imageUrl = image.getValue(String::class.java)
                        if (!imageUrl.isNullOrEmpty()) {
                            imageUrls.add(imageUrl)
                        }
                    }

                    displayImages(imageUrls)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching data: ${error.message}")
            }
        })
    }

    private fun displayImages(imageUrls: List<String>) {
        gridLayout.removeAllViews()

        for (imageUrl in imageUrls) {
            val cardView = CardView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(250, 250)
                radius = 20f
                elevation = 6f
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Convert Base64 string to Bitmap
            val bitmap = decodeBase64ToBitmap(imageUrl)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                Log.e("ImageDecodeError", "Failed to decode Base64 image")
            }

            cardView.addView(imageView)
            gridLayout.addView(cardView)
        }
    }

    private fun decodeBase64ToBitmap(encodedString: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
        } catch (e: Exception) {
            Log.e("Base64Error", "Error decoding Base64 string: ${e.message}")
            null
        }
    }
}
