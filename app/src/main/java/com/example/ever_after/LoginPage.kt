package com.example.ever_after

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginPage : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogle = findViewById(R.id.btnGoogle)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("723409551747-5fahg8baue3qjc2e84vch6f738k4ksqv.apps.googleusercontent.com") // 🔥 Replace with your Web Client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            val intent = Intent(this, login_page::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

        btnGoogle.setOnClickListener {
            loginWithGoogle()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    // ✅ FCM token update karne ke liye function call
                    user?.uid?.let { updateFCMToken(it) }
                    checkUserData()
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ✅ Login hone ke baad Firestore me FCM Token Store Karna
    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "❌ Failed to get token", task.exception)
                    return@addOnCompleteListener
                }

                val fcmToken = task.result
                Log.d("FCM", "✅ Generated FCM Token: $fcmToken") // Check Logcat

                val userRef = db.collection("Users").document(userId)
                userRef.update("fcmToken", fcmToken)
                    .addOnSuccessListener {
                        Log.d("FCM", "✅ FCM token updated in Firestore.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "❌ Failed to update FCM token: ${e.message}")
                    }
            }
    }

    private fun checkUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            navigateTo(LoginPage::class.java)
            return
        }

        userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Images")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.child("Image1").getValue(String::class.java)

                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    navigateTo(BottomNavigation::class.java)  // Image node hai, to direct BottomNavigation par jayega
                } else {
                    navigateTo(detailsPage::class.java)  // Agar login nahi kiya to LoginPage par jayega
                }
            }

            override fun onCancelled(error: DatabaseError) {
                navigateTo(LoginPage::class.java)
            }
        })
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
        finish()
    }
}
