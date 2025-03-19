package com.example.ever_after

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth

class login_page : AppCompatActivity() {
    private lateinit var btnLogin : Button
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText

    private lateinit var share : SharePrefrence

    private lateinit var loadingDialog : Dialog
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
        password = findViewById(R.id.etPassword)

        setupLoadingDialog()

        btnLogin.setOnClickListener{
            loginUser()
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setContentView(R.layout.loading_dialog)  // Custom Lottie Layout
        loadingDialog.setCancelable(false)  // Disable outside touch
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent BG
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
                    loadingDialog.dismiss()
                    share.loginState(true)
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, detailsPage::class.java)
                    startActivity(intent)
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}