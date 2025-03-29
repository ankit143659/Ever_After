package com.example.ever_after

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class Registration : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var realtimeDb: FirebaseDatabase

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var phno: EditText
    private lateinit var pass: EditText
    private lateinit var cpass: EditText
    private lateinit var btnRegister: Button


    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        realtimeDb = FirebaseDatabase.getInstance()

        // Initialize UI Elements
        name = findViewById(R.id.name)
        email = findViewById(R.id.etEmail)
        phno = findViewById(R.id.etPhone)
        pass = findViewById(R.id.etPassword)
        cpass = findViewById(R.id.etCPassword)
        btnRegister = findViewById(R.id.btnRegister)


        setupLoadingDialog()

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val Name = name.text.toString().trim()
        val Email = email.text.toString().trim()
        val PhNo = phno.text.toString().trim()
        val Pass = pass.text.toString().trim()
        val Cpass = cpass.text.toString().trim()

        // **Validation**
        if (Name.isEmpty()) {
            name.error = "Enter your name"
            return
        }

        if (Email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            email.error = "Enter a valid email"
            return
        }

        if (PhNo.isEmpty() || PhNo.length != 10) {
            phno.error = "Enter a valid 10-digit phone number"
            return
        }

        if (Pass.isEmpty()) {
            pass.error = "Password cannot be empty"
            return
        }

        if (Pass.length < 8) {
            pass.error = "Password must be at least 8 characters long"
            return
        }

        if (!Pass.matches(".*[A-Z].*".toRegex())) {
            pass.error = "Password must contain at least one uppercase letter"
            return
        }

        if (!Pass.matches(".*[a-z].*".toRegex())) {
            pass.error = "Password must contain at least one lowercase letter"
            return
        }

        if (!Pass.matches(".*\\d.*".toRegex())) {
            pass.error = "Password must contain at least one number"
            return
        }

        if (!Pass.matches(".*[@#\$%^&+=!].*".toRegex())) {
            pass.error = "Password must contain at least one special character (@#\$%^&+=!)"
            return
        }

        if (Pass != Cpass) {
            cpass.error = "Passwords do not match"
            return
        }


        if (Pass != Cpass) {
            cpass.error = "Passwords do not match"
            return
        }

        loadingDialog.show()


        // **Register User in Firebase Authentication**
        auth.createUserWithEmailAndPassword(Email, Pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                saveUserData(userId, Name, PhNo, Email)
            } else {
                loadingDialog.dismiss()
                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setContentView(R.layout.loading_dialog)  // Custom Lottie Layout
        loadingDialog.setCancelable(false)  // Disable outside touch
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent BG
    }

    private fun saveUserData(userId: String, name: String, phone: String, email: String) {
        val user = hashMapOf(
            "userId" to userId,
            "name" to name,
            "phone" to phone,
            "email" to email
        )

        // **Save in Firestore**
        firestore.collection("Users").document(userId).set(user)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show()
                realtimeDb.getReference("Users").child(userId).setValue(user)
                val intent = Intent(this,login_page::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // **Save in Realtime Database (Optional)**

    }
}
