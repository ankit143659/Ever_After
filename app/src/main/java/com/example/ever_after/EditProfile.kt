package com.example.ever_after

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class EditProfile : AppCompatActivity() {
    private lateinit var name : EditText
    private lateinit var phNo : EditText
    private lateinit var dob : EditText
    private lateinit var meetingPerson : EditText
    private lateinit var btnSaveChanges : MaterialButton

    private lateinit var man : MaterialRadioButton
    private lateinit var woman : MaterialRadioButton

    private lateinit var database: FirebaseDatabase
    private lateinit var database2: DatabaseReference

    private lateinit var profile_image : ImageView
    private lateinit var loadingDialog: Dialog

    private lateinit var changePhoto : TextView

    private lateinit var backBtn : ImageButton

    private  var imageString : String = ""

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    loadingDialog.show()
                  profile_image.setImageURI(uri)
                    processImage(uri)
                }
            }
        }

    private  var gender : String = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        name = findViewById(R.id.etName)
        phNo = findViewById(R.id.etPhone)
        dob = findViewById(R.id.etDob)
        man = findViewById(R.id.rbMale)
        woman = findViewById(R.id.rbFemale)

        backBtn = findViewById(R.id.btnBack)

        backBtn.setOnClickListener{
            finish()
        }

        changePhoto = findViewById(R.id.changePhoto)

        changePhoto.setOnClickListener{

            openImagePicker()
        }


        val auth = FirebaseAuth.getInstance().currentUser
        val userId = auth?.uid

        database2 = userId?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }!!
        profile_image = findViewById(R.id.profile_image)


        setupLoadingDialog()
        fetchUserData()


        man.setOnCheckedChangeListener{_,ischeked->
            if (ischeked){
                gender = "Man"
            }
        }

        woman.setOnCheckedChangeListener{_,isChecked ->
            if (isChecked){
                gender = "Woman"
            }
        }
        meetingPerson = findViewById(R.id.etMeetingPerson)
        database = FirebaseDatabase.getInstance()

        btnSaveChanges = findViewById(R.id.btnSaveChanges)

        btnSaveChanges.setOnClickListener {
            saveData()
        }
    }


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }


    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setContentView(R.layout.loading_dialog)  // Custom Lottie Layout
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun processImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = getBitmapFromUri(uri)
            val bitmapString = bitmap?.let { encodeToBase64(it) }
            imageString = bitmapString!!
            loadingDialog.dismiss()
        }
    }


    private suspend fun getBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error loading bitmap", e)
            null
        }
    }

    private suspend fun encodeToBase64(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val scaledBitmap = compressBitmap(bitmap, 800) // Resize and compress
        ByteArrayOutputStream().apply {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, this) // JPEG 60% quality
        }.toByteArray().let { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    private fun compressBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / aspectRatio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }



    private fun fetchUserData() {
        loadingDialog.show()
        database2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val Name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                    val Phone = snapshot.child("phone").getValue(String::class.java) ?: "N/A"
                    val Gender = snapshot.child("Details").child("Gender").getValue(String::class.java)
                    val MeetingPerson = snapshot.child("Details").child("MeetingPerson").getValue(String::class.java)
                    val Dob = snapshot.child("Details").child("DOB").getValue(String::class.java)

                    name.setText(Name)
                    phNo.setText(Phone)
                    if (Gender=="Man"){
                        man.isChecked = true
                    }else{
                        woman.isChecked = true
                    }

                    meetingPerson.setText(MeetingPerson)
                    dob.setText(Dob)


                    imageString = snapshot.child("Images/Image1").getValue(String::class.java)!!
                    profile_image.setImageBitmap(imageString.let { decodeBase64ToBitmap(it) })

                    loadingDialog.dismiss()

                }else{
                    loadingDialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismiss()
                Log.e("FirebaseError", "Error fetching data: ${error.message}")
            }
        })
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

    private fun saveData() {
        gender = if (man.isChecked) "Man" else "Woman"

        val Name = name.text.toString().trim()
        val Phno = phNo.text.toString().trim()
        val Dob = dob.text.toString().trim()
        val Meeting = meetingPerson.text.toString().trim()

        val auth = FirebaseAuth.getInstance().currentUser
        val userId = auth?.uid

        if (Name.isEmpty()) {
            Toast.makeText(this, "Please Enter Name", Toast.LENGTH_SHORT).show()
            return
        }
        if (Phno.isEmpty()) {
            Toast.makeText(this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show()
            return
        }
        if (Dob.isEmpty()) {
            Toast.makeText(this, "Please Enter DOB", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidDate(Dob)) {
            Toast.makeText(this, "Please enter Date in dd/MM/yyyy format", Toast.LENGTH_SHORT).show()
            return
        }
        if (Meeting.isEmpty()) {
            Toast.makeText(this, "Please Enter Meeting person field", Toast.LENGTH_SHORT).show()
            return
        }
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId != null) {
            loadingDialog.show()
            val dbRef = database.getReference("Users").child(userId).child("Details")
            val dbRef2 = database.getReference("Users").child(userId)

            val imageRef = database.getReference("Users").child(userId).child("Images")
            imageRef.child("Image1").setValue(imageString)

            val userDetails = mapOf(
                "DOB" to Dob,
                "Gender" to gender,
                "MeetingPerson" to Meeting
            )

            val userDetails2 = mapOf(
                "name" to Name,
                "phone" to Phno
            )

            dbRef.updateChildren(userDetails).addOnCompleteListener { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    dbRef2.updateChildren(userDetails2).addOnCompleteListener { task ->
                        loadingDialog.dismiss()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to Update Profile", Toast.LENGTH_SHORT).show()
                            Log.e("FirebaseError", "Error updating details: ${task.exception?.message}")
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to Update Profile", Toast.LENGTH_SHORT).show()
                    Log.e("FirebaseError", "Error updating details: ${task.exception?.message}")
                }
            }


        }
    }


    fun isValidDate(input: String, format: String = "dd/MM/yyyy"): Boolean {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.isLenient = false // Ensures strict validation
            dateFormat.parse(input)
            true
        } catch (e: Exception) {
            false
        }
    }
}