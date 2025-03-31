package com.example.ever_after

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class UserHomeAdapter(private val userList: MutableList<UserModel>,private val context: Context) :
    RecyclerView.Adapter<UserHomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nameText: TextView = view.findViewById(R.id.user_name_age)
        val religion: TextView = view.findViewById(R.id.religion)
        val match_percentage: TextView = view.findViewById(R.id.match_percentage)
//        val interestsLayout: LinearLayout = view.findViewById(R.id.interestsLayout)
        val like_button : FloatingActionButton = view.findViewById(R.id.like_button)
        val Chat_Button: MaterialButton = view.findViewById(R.id.detail_button)
        val Dis_like: FloatingActionButton = view.findViewById(R.id.dislike_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.demo, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        val age = calculateAge(user.DOB)
        holder.nameText.text = if (age >= 0) "${user.name}, $age" else "${user.name}, Invalid DOB"
       holder.religion.text = "${user.distance} Km away"

      holder.match_percentage.text = "${user.matchPercentage}%"
        holder.Chat_Button.setOnClickListener {
            val fragment = ProfileBottomSheetFragment.newInstance(user.userId)
            fragment.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "profileBottomSheet")
        }

        val layoutParams = holder.itemView.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = layoutParams

        // **Decode Base64 Image**
        val profileBitmap = decodeBase64ToBitmap(user.Image1)

        if (profileBitmap != null) {
            holder.profileImage.setImageBitmap(profileBitmap)
        } else {
            holder.profileImage.setImageResource(R.drawable.tony) // Use a default image
        }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef =
            currentUserId?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it).child("DislikedUsers")
            }


//        // **Dynamically Add Interests**
//        holder.interestsLayout.removeAllViews()
//        user.Interest.split(",").map { it.trim() }.forEach { interest ->
//            val textView = TextView(holder.itemView.context).apply {
//                text = interest
//                setPadding(16, 8, 16, 8)
//                setBackgroundResource(R.drawable.tag_bg)
//                setTextColor(Color.WHITE)
//                textSize = 14f
//                setTypeface(typeface, Typeface.BOLD)
//            }
//            holder.interestsLayout.addView(textView)
//        }

// ✅ **Check Status Before Showing Buttons**
        currentUserId?.let {
            checkRequestStatus(
                it,
                user.userId,
                holder.like_button,
                holder.Dis_like
            )
        }

        // **Step 2: Handle Click Event for Send Request Button**
        holder.like_button.setOnClickListener {
            if (user.userId.isNotEmpty()) {
                currentUserId?.let { it1 ->
                    sendRequest(
                        it1,
                        user.userId,
                        holder.like_button,
                       holder.Dis_like
                    )
                } // ✅ Sender = Current User, Receiver = Selected User
            } else {
                Log.e("Adapter", "Receiver ID is empty!")
            }
        }
        holder.Dis_like.setOnClickListener {
            if (databaseRef != null) {
                Log.d("DislikeUser", "Disliking User ID: ${user.userId}")

                databaseRef.child(user.userId).setValue(true)
                    .addOnSuccessListener {
                        Log.d("DislikeUser", "Before Removal - Position: $position, List Size: ${userList.size}")

                        if (position in userList.indices) { // Ensure valid position
                            userList.removeAt(position)
                            notifyItemRemoved(position)

                            // RecyclerView shifting issue fix
                            notifyItemRangeChanged(position, userList.size - position)

                            Log.d("DislikeUser", "User ${user.userId} disliked successfully")
                        } else {
                            Log.e("DislikeUser", "Invalid position: $position, Size: ${userList.size}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DislikeUser", "Error disliking user: ${e.message}")
                    }
            }
        }

    }

    override fun getItemCount() = userList.size

    fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrEmpty()) {
            Log.e("DecodeError", "Base64 string is null or empty")
            return null
        }

        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("DecodeError", "Error decoding Base64: ${e.message}")
            null
        }
    }


    private fun sendRequest(
        senderId: String,
        receiverId: String,
        sendRequestButton: FloatingActionButton,
        Dis_like: FloatingActionButton
    ) {
        val database = FirebaseDatabase.getInstance().reference

        val requestKey = database.child("Users").child(receiverId).child("Requests").push().key

        if (requestKey != null) {
            val requestData = mapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "status" to "pending"
            )

            val updates = hashMapOf<String, Any>(
                "/Users/$receiverId/Requests/$requestKey" to requestData
            )

            database.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(
                    sendRequestButton.context,
                    "Request Sent",
                    Toast.LENGTH_SHORT
                ).show()

                sendNotificationToProvider(receiverId, "New Request", "New Friend Requested")

                // ✅ Sirf iss receiver ke liye button disable karo
                sendRequestButton.isEnabled = false

                checkRequestStatus(senderId, receiverId, sendRequestButton, Dis_like)
            }.addOnFailureListener { e ->
                Toast.makeText(
                    sendRequestButton.context,
                    "Failed to send request: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                sendRequestButton.context,
                "Failed to generate request key!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    private fun checkRequestStatus(
        senderId: String,
        receiverId: String,
        sendRequestButton: FloatingActionButton,
        Dis_like: FloatingActionButton
    ) {
        val senderRequestRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(receiverId)
            .child("Requests")

        senderRequestRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (requestSnapshot in snapshot.children) {
                    val request = requestSnapshot.getValue(RequestModel::class.java)
                    val senderId = requestSnapshot.child("senderId").getValue(String::class.java)
                    val auth = FirebaseAuth.getInstance().currentUser
                    val userId = auth?.uid
                    if (request?.receiverId == receiverId && userId == senderId) {
                        if (request.status == "accepted") {
                            Dis_like.visibility = View.GONE  // ✅ Chat Button Show
                            sendRequestButton.visibility = View.GONE  // ❌ Hide Send Request Button
                        } else if (request.status == "pending") {
                           // sendRequestButton.text = "Request Sent"
                        sendRequestButton.isEnabled = false
                        }
                        else{
                            Dis_like.visibility = View.VISIBLE  // ✅ Chat Button Show
                            sendRequestButton.visibility = View.VISIBLE
                        }
                        return  // ✅ Matching request mil gaya, loop se exit ho jao
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CheckStatus", "Error: ${error.message}")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAge(dob: String): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // 👈 DOB format
            val birthDate = LocalDate.parse(dob, formatter) // 👈 Convert String to Date
            val currentDate = LocalDate.now() // 👈 Get today's date
            ChronoUnit.YEARS.between(birthDate, currentDate).toInt() // 👈 Calculate Age
        } catch (e: Exception) {
            -1 // 👈 Return -1 if error occurs (Invalid DOB format)
        }
    }
    private fun sendNotificationToProvider(receiverId: String, title: String, message: String) {
        val db = FirebaseDatabase.getInstance()
        db.getReference("Users").child(receiverId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fcmToken = document.child("fcmToken").value.toString()
                    Log.d("FCM", fcmToken)
                    if (!fcmToken.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            sendFCMNotification(fcmToken, title, message)
                        }
                    } else {
                        Log.e("FCM", "Provider FCM Token is Empty")
                    }
                } else {
                    Log.e("FCM", "Provider Not Found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to fetch provider data: ${e.message}")
            }
    }

    private suspend fun sendFCMNotification(providerToken: String, title: String, body: String) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/ever-after-2c973/messages:send"

        val accessToken = TokenManager.getAccessToken(context) // Get Firebase Access Token
        if (accessToken == null) {
            Log.e("FCM", "Failed to generate access token")
            return
        }

        val jsonObject = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", providerToken)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
            })
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(fcmUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = OkHttpClient().newCall(request).execute()
                Log.d("FCM", "Notification Response: ${response.body?.string()}")
            } catch (e: Exception) {
                Log.e("FCM", "Error sending notification: ${e.message}")
            }
        }
    }

    object TokenManager {
        private var cachedToken: String? = null
        private var tokenExpiryTime: Long = 0L

        suspend fun getAccessToken(context: Context): String? {
            val currentTime = System.currentTimeMillis()

            // ✅ Pehle se token hai aur expire nahi hua, to wahi return karo
            if (cachedToken != null && currentTime < tokenExpiryTime) {
                Log.d("FCM", "Using Cached Token: $cachedToken")
                return cachedToken
            }

            return withContext(Dispatchers.IO) {
                try {
                    val jsonStream = context.assets.open("service-account.json")
                    val googleCredentials = GoogleCredentials.fromStream(jsonStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                    googleCredentials.refreshIfExpired()
                    val newToken = googleCredentials.accessToken?.tokenValue
                    val expiresIn = googleCredentials.accessToken?.expirationTime?.time ?: 0L // ✅ Fixed

                    if (newToken != null) {
                        cachedToken = newToken
                        tokenExpiryTime = expiresIn
                    }

                    Log.d("FCM", "Generated New Token: $newToken")
                    newToken
                } catch (e: IOException) {
                    Log.e("FCM", "Error getting access token: ${e.message}")
                    null
                }
            }
        }
    }}
