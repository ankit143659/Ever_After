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

class dataViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    private val _gender = MutableLiveData<String>()
    private val _purpose = MutableLiveData<String>()
    private val _meetGender = MutableLiveData<String>()
    private val _height = MutableLiveData<String>()
    private val _selectedOptions = MutableLiveData<List<String>>()  // Store selected options
    val selectedOptions: LiveData<List<String>> = _selectedOptions
    val name: LiveData<String> get() = _name
    val gender: LiveData<String> get() = _gender
    val purpose: LiveData<String> get() = _purpose
    val genderMeeet: LiveData<String> get() = _meetGender
    val height : LiveData<String> get() = _height

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
        if (name=="Interest"){
            database.child("Interest").setValue("$currentSet")
        }else if (name =="Value"){
            database.child("ValueInPerson").setValue("$currentSet")
        }

        Log.d("ToggleInterest", "Updated Interests: $currentSet")
    }



    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    fun updateName(newName: String) {
        if (newName.isNotBlank()) {  // Empty name store na ho
            _name.value = newName
        }
    }

    fun updateDate(dd: String, mm: String, yy: String) {
        if (dd.length == 2 && mm.length == 2 && yy.length == 4) {
            _date.value = "$dd/$mm/$yy"
        }
    }

    fun isDataValid(): Boolean {
        return !(_name.value.isNullOrEmpty() || _date.value.isNullOrEmpty())
    }

    fun updateGender(gender:String){
        if (gender.isNotBlank()){
            _gender.value = gender
        }
    }

    fun gendervalueSelected():Boolean{
        return !(_gender.value.isNullOrEmpty())
    }


    fun updatePurpose(purpose:String){
        if (purpose.isNotBlank()){
            _purpose.value = purpose
        }
    }

    fun purposeValue():Boolean{
        return !(_purpose.value.isNullOrEmpty())
    }

    fun genderMeet(meet:String){
        if (meet.isNotBlank()){
            _meetGender.value = meet
        }
    }

    fun genderMeetiing():Boolean{
        return !(_meetGender.value.isNullOrEmpty())
    }

    fun updateHope(option : List<String>){
        _selectedOptions.value = option
    }

    fun updateHeight(heightt : String){
        if (heightt.isNotBlank()){
            _height.value = heightt
        }
    }

    fun checkHeight() : Boolean{
        return !(_height.value.isNullOrEmpty())
    }

    fun  checkInterest() : Boolean{
        return _selectedInterests.value.isNullOrEmpty()
    }

    fun checkImage() : Boolean{
        return _imageBitmaps.value.isNullOrEmpty()
    }


}
