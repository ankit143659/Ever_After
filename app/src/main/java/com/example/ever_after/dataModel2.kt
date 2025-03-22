package com.example.ever_after

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class dataModel2 : ViewModel() {


    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val _selectedInterests = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedInterests: LiveData<MutableSet<String>> = _selectedInterests

    private val _imageBitmaps = MutableLiveData<List<String>>(emptyList())
    val imageBitmaps: LiveData<List<String>> get() = _imageBitmaps

    suspend fun updateImage(newBitmap: Bitmap, position: Int): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return false

        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId)
            .child("Images")
            .child("Image${position + 1}")

        val compressedBase64 = bitmapToBase64(newBitmap, 40)

        return try {
            databaseRef.setValue(compressedBase64).await()  // 🔹 Coroutine suspend karega
            Log.d("FirebaseSuccess", "Image updated successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseError", "Failed to update image: ${e.message}")
            false
        }
    }


    // 🔥 **Bitmap ko Base64 me convert karne ka helper function**
    fun bitmapToBase64(bitmap: Bitmap, quality: Int): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)  // 🔹 Compress karega
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)  // 🔹 NO_WRAP = Extra new lines nahi ayengi, size aur kam hoga
    }





    /*
        // Function to convert Bitmap to Base64 String
        private fun bitmapToBase64(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }*/


    fun toggleInterest(item: String, maxSelection: Int,name : String) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Current User ka UID
            database = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Details")
        } else {
            database = FirebaseDatabase.getInstance().getReference("Users").child("Unknown")
        }

        val currentSet = _selectedInterests.value?.toMutableSet() ?: linkedSetOf()  // 👈 Ensure ordered set

        if (currentSet.contains(item)) {
            currentSet.remove(item)
        } else {
            if (currentSet.size >= maxSelection) {
                val firstItem = currentSet.first()  // 👈 Remove oldest selection
                currentSet.remove(firstItem)
            }
            currentSet.add(item)
        }

        _selectedInterests.value = currentSet.toMutableSet()
        database.child(name).setValue("$currentSet")

        Log.d("ToggleInterest", "Updated Interests: $currentSet")
    }



    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    fun  checkInterest() : Boolean{
        return _selectedInterests.value.isNullOrEmpty()
    }

    fun checkImage() : Boolean{
        return _imageBitmaps.value.isNullOrEmpty()
    }


}
