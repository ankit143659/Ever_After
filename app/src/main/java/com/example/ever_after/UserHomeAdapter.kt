package com.example.ever_after

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserHomeAdapter(private val userList: List<UserModel>) :
    RecyclerView.Adapter<UserHomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nameText: TextView = view.findViewById(R.id.profile_name)
        val locationText: TextView = view.findViewById(R.id.profile_location)
        val interestsLayout: LinearLayout = view.findViewById(R.id.interests_layout)
        val sendRequestButton: Button = view.findViewById(R.id.send_request_button)
        val Chat_Button: ImageButton = view.findViewById(R.id.chat_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.demo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = "${user.name}, ${user.DOB}"
        holder.locationText.text = user.Communities

        // **Decode Base64 Image**
        if (!user.Image1.isNullOrEmpty()) {
            Log.d("UserAdapter", "Base64 Image Length: ${user.Image1!!.length}")
            holder.profileImage.setImageBitmap(user.Image1?.let { decodeBase64ToBitmap(it) })
        }
        else{
            holder.profileImage.setImageResource(R.drawable.tony)
        }
        val currentUserId= FirebaseAuth.getInstance().currentUser?.uid

        // **Dynamically Add Interests**
        holder.interestsLayout.removeAllViews()
        user.Interest.split(" ").forEach { interest ->
            val textView = TextView(holder.itemView.context).apply {
                text = interest
                setPadding(16, 8, 16, 8)
                setBackgroundResource(R.drawable.tag_bg)
                setTextColor(Color.WHITE)
            }
            holder.interestsLayout.addView(textView)
        }
// ✅ **Check Status Before Showing Buttons**
        currentUserId?.let { checkRequestStatus(it, user.userId, holder.Chat_Button, holder.sendRequestButton) }

        // **Step 2: Handle Click Event for Send Request Button**
        holder.sendRequestButton.setOnClickListener {
            if (user.userId.isNotEmpty()) {
                currentUserId?.let { it1 -> sendRequest(it1, user.userId,holder.sendRequestButton,holder.Chat_Button) } // ✅ Sender = Current User, Receiver = Selected User
            } else {
                Log.e("Adapter", "Receiver ID is empty!")
            }
        }
    }
    override fun getItemCount() = userList.size
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    private fun sendRequest(senderId: String, receiverId: String, sendRequestButton: Button, chatButton: ImageButton) {
        val requestRef = FirebaseDatabase.getInstance().getReference("Requests")

        val requestData = mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "status" to "pending"  // **Directly Accepted Karna**
        )

        requestRef.push().setValue(requestData).addOnSuccessListener {
            Toast.makeText(sendRequestButton.context, "Request Sent", Toast.LENGTH_SHORT).show()
            sendRequestButton.text = "Request Sent"
            sendRequestButton.isEnabled = false
            checkRequestStatus(senderId, receiverId, chatButton, sendRequestButton)  // ✅ Status Update
        }.addOnFailureListener {
            Toast.makeText(sendRequestButton.context, "Failed to send request!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkRequestStatus(senderId: String, receiverId: String, chatButton: ImageButton, sendRequestButton: Button) {
        val requestRef = FirebaseDatabase.getInstance().getReference("Requests")

        requestRef.orderByChild("senderId").equalTo(senderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(RequestModel::class.java)
                        if (request?.receiverId == receiverId) {
                            if (request.status == "accepted") {
                                chatButton.visibility = View.VISIBLE  // ✅ Chat Button Show
                                sendRequestButton.visibility = View.GONE  // ❌ Hide Send Request Button
                            } else if (request.status == "pending") {
                                sendRequestButton.text = "Request Sent"
                                sendRequestButton.isEnabled = false
                            }
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CheckStatus", "Error: ${error.message}")
                }
            })
    }


}
