package com.example.ever_after

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class login_page : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var Forgot: TextView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var share: SharePrefrence
    private lateinit var loadingDialog: Dialog
    private val database = FirebaseDatabase.getInstance().reference // ✅ Realtime Database

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page2)

        btnLogin = findViewById(R.id.btnLogin)
        lottieAnimationView = findViewById(R.id.lotti)
        lottieAnimationView.playAnimation()

        share = SharePrefrence(this)
        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.etEmail)
        Forgot = findViewById(R.id.tvForgotPassword)
        password = findViewById(R.id.etPassword)

        setupLoadingDialog()
        Forgot.setOnClickListener {
            showForgotPasswordDialog()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setContentView(R.layout.loading_dialog)  // Custom Lottie Layout
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")

        val input = EditText(this)
        input.hint = "Enter your email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Send") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginUser() {
        val Email = email.text.toString().trim()
        val Pass = password.text.toString().trim()

        if (!isValidEmail(Email)) {
            email.error = "Enter a valid email"
            return
        }

        if (Pass.isEmpty()) {
            password.error = "Password cannot be empty"
            return
        }

        if (Pass.length < 6) {
            password.error = "Password must be at least 6 characters"
            return
        }

        loadingDialog.show()

        auth.signInWithEmailAndPassword(Email, Pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        updateFCMToken(user.uid)  // 🔥 Store FCM token in Realtime DB
                    }
                    loadingDialog.dismiss()
                    share.loginState(true)
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, detailsPage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("LoginActivity", "❌ FCM token generation failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            val userRef = database.child("Users").child(userId) // ✅ Store in Realtime DB

            val tokenMap = mapOf("fcmToken" to fcmToken)
            userRef.updateChildren(tokenMap)
                .addOnSuccessListener {
                    Log.d("LoginActivity", "✅ FCM token updated in Realtime Database.")
                }
                .addOnFailureListener { e ->
                    Log.e("LoginActivity", "❌ Failed to update FCM token: ${e.message}")
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}