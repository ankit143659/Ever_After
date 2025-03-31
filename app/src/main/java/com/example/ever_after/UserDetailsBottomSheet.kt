package com.example.ever_after

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserDetailsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var userImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userGender: TextView
    private lateinit var userDOB: TextView
    private lateinit var userHeight: TextView
    private lateinit var userInterest: TextView
    private lateinit var userSmokingStatus: TextView
    private lateinit var userDrinkingStatus: TextView

    companion object {
        fun newInstance(userData: HashMap<String, String>) = UserDetailsBottomSheet().apply {
            arguments = Bundle().apply {
                for ((key, value) in userData) {
                    putString(key, value)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.request_bottomsheet    , container, false)

        userImage = view.findViewById(R.id.userProfileImage)
        userName = view.findViewById(R.id.userName)
        userGender = view.findViewById(R.id.userGender)
        userDOB = view.findViewById(R.id.userDOB)
        userHeight = view.findViewById(R.id.userHeight)
        userInterest = view.findViewById(R.id.userInterest)
        userSmokingStatus = view.findViewById(R.id.userSmokingStatus)
        userDrinkingStatus = view.findViewById(R.id.userDrinkingStatus)

        arguments?.let { bundle ->
            userName.text = bundle.getString("name", "N/A")
            userGender.text = "Gender: ${bundle.getString("Gender", "N/A")}"
            userDOB.text = "DOB: ${bundle.getString("DOB", "N/A")}"
            userHeight.text = "Height: ${bundle.getString("Height", "N/A")}"
            userInterest.text = "Interests: ${bundle.getString("Interest", "N/A")}"
            userSmokingStatus.text = "Smoking: ${bundle.getString("SmokingStatus", "N/A")}"
            userDrinkingStatus.text = "Drinking: ${bundle.getString("DrinkingStatus", "N/A")}"

            val imageBase64 = bundle.getString("Image", "")
            if (!imageBase64.isNullOrEmpty()) {
                userImage.setImageBitmap(decodeBase64ToBitmap(imageBase64))
            }
        }

        return view
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
