package com.example.ever_after

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class UserHomeAdapter(private val userList: MutableList<UserModel>,private val context: Context) :
    RecyclerView.Adapter<UserHomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nameText: TextView = view.findViewById(R.id.profile_name)
        val Community: TextView = view.findViewById(R.id.Communitity)
        val match_percentage: TextView = view.findViewById(R.id.match_percentage)
        val interestsLayout: LinearLayout = view.findViewById(R.id.interestsLayout)
        val sendRequestButton: Button = view.findViewById(R.id.send_request_button)
        val Chat_Button: ImageButton = view.findViewById(R.id.chat_button)
        val Dis_like: ImageButton = view.findViewById(R.id.dis_like)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.demo, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        val age = calculateAge(user.DOB)
        holder.nameText.text = if (age >= 0) "${user.name}, $age" else "${user.name}, Invalid DOB"
        holder.Community.text = user.Communities
        holder.match_percentage.text = "${user.matchPercentage}%"

        val layoutParams = holder.itemView.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = layoutParams

        // **Decode Base64 Image**
        if (!user.Image1.isNullOrEmpty()) {
            Log.d("UserAdapter", "Base64 Image Length: ${user.Image1!!.length}")
            holder.profileImage.setImageBitmap(user.Image1?.let { decodeBase64ToBitmap(it) })
        } else {
            holder.profileImage.setImageResource(R.drawable.tony)
        }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef =
            currentUserId?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it).child("DislikedUsers")
            }


        // **Dynamically Add Interests**
        holder.interestsLayout.removeAllViews()
        user.Interest.split(",").map { it.trim() }.forEach { interest ->
            val textView = TextView(holder.itemView.context).apply {
                text = interest
                setPadding(16, 8, 16, 8)
                setBackgroundResource(R.drawable.tag_bg)
                setTextColor(Color.WHITE)
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
            }
            holder.interestsLayout.addView(textView)
        }

// ✅ **Check Status Before Showing Buttons**
        currentUserId?.let {
            checkRequestStatus(
                it,
                user.userId,
                holder.Chat_Button,
                holder.sendRequestButton,
                holder.Dis_like
            )
        }

        // **Step 2: Handle Click Event for Send Request Button**
        holder.sendRequestButton.setOnClickListener {
            if (user.userId.isNotEmpty()) {
                currentUserId?.let { it1 ->
                    sendRequest(
                        it1,
                        user.userId,
                        holder.sendRequestButton,
                        holder.Chat_Button,holder.Dis_like
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
                        if (position >= 0 && position < userList.size) { // Safe check
                            userList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, userList.size)
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

    fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun sendRequest(
        senderId: String,
        receiverId: String,
        sendRequestButton: Button,
        chatButton: ImageButton,
        Dis_like:ImageButton
    ) {
        val database = FirebaseDatabase.getInstance().reference

        // Unique auto-generated key for the request (same for both users)
        val requestKey = database.child("Users").child(senderId).child("Requests").push().key

        if (requestKey != null) {
            // Request ka data
            val requestData = mapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "status" to "pending"
            )

            // Correct path for Firebase update
            val updates = hashMapOf<String, Any>(
                "/Users/$receiverId/Requests/$requestKey" to requestData  // Receiver ke under
            )

            database.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(
                    sendRequestButton.context,
                    "Request Sent",
                    Toast.LENGTH_SHORT
                ).show()
                sendRequestButton.text = "Request Sent"
                sendRequestButton.isEnabled = false
                checkRequestStatus(
                    senderId,
                    receiverId,
                    chatButton,
                    sendRequestButton,
                    Dis_like
                )  // ✅ Status Update
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
        chatButton: ImageButton,
        sendRequestButton: Button,
        Dis_like: ImageButton
    ) {
        val senderRequestRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(senderId)
            .child("Requests")

        senderRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (requestSnapshot in snapshot.children) {
                    val request = requestSnapshot.getValue(RequestModel::class.java)
                    if (request?.receiverId == receiverId) {
                        if (request.status == "accepted") {
                            chatButton.visibility = View.VISIBLE  // ✅ Chat Button Show
                            Dis_like.visibility = View.GONE  // ✅ Chat Button Show
                            sendRequestButton.visibility = View.GONE  // ❌ Hide Send Request Button
                        } else if (request.status == "pending") {
                            sendRequestButton.text = "Request Sent"
                            sendRequestButton.isEnabled = false
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


}
