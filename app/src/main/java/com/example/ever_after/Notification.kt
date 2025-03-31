package com.example.ever_after

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Notification : Fragment() {

    private lateinit var requestRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var requestContainer: LinearLayout
    private val TAG = "NotificationFragment"
    private var imageString : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        requestContainer = view.findViewById(R.id.requestContainer)
        listenForRequests()
        return view
    }

    private fun listenForRequests() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        requestRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId).child("Requests")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId).child("Friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(friendSnapshot: DataSnapshot) {
                    val friendIds = friendSnapshot.children.mapNotNull { it.key }.toSet()
                    requestRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(requestSnapshot: DataSnapshot) {
                            requestContainer.removeAllViews()
                            requestSnapshot.children.forEach { request ->
                                val senderId = request.child("senderId").getValue(String::class.java) ?: return@forEach
                                fetchSenderDetails(senderId, request.key!!, friendIds.contains(senderId))
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Failed to fetch requests: ${error.message}")
                        }
                    })
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch friend list: ${error.message}")
                }
            })
    }

    private fun fetchSenderDetails(senderId: String, requestId: String, isAccepted: Boolean) {
        userRef.child(senderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val senderName = snapshot.child("name").getValue(String::class.java) ?: return
                val imageString2 = snapshot.child("Images").child("Image1").getValue(String::class.java)
                if (imageString2 != null) {
                    imageString = imageString2
                }
                val bitmap = imageString2?.let { decodeBase64ToBitmap(it) }
                addRequestUI(senderName, senderId, requestId, isAccepted, bitmap)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch sender details: ${error.message}")
            }
        })
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Base64 decoding error: ${e.message}")
            null
        }
    }

    private fun addRequestUI(senderName: String, senderId: String, requestId: String, isAccepted: Boolean, image: Bitmap?) {
        if (!isAdded) return

        requireActivity().runOnUiThread {
            val requestView = LayoutInflater.from(requireContext()).inflate(R.layout.request_design, requestContainer, false)
            val usernameText = requestView.findViewById<TextView>(R.id.username)
            val acceptButton = requestView.findViewById<MaterialCardView>(R.id.btn_accept)
            val rejectButton = requestView.findViewById<MaterialCardView>(R.id.btn_reject)
            val statusText = requestView.findViewById<TextView>(R.id.status)
            val profileImage = requestView.findViewById<ImageView>(R.id.profile_image)

            profileImage.setImageBitmap(image)
            usernameText.text = senderName

            if (isAccepted) {
                acceptButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
                statusText.text = "Friend"
                statusText.visibility = View.VISIBLE
            } else {
                acceptButton.visibility = View.VISIBLE
                rejectButton.visibility = View.VISIBLE
                statusText.visibility = View.GONE

                acceptButton.setOnClickListener { updateRequestStatus(requestId, senderId, acceptButton, rejectButton, statusText) }
                rejectButton.setOnClickListener { rejectFriendRequest(requestId, acceptButton, rejectButton, statusText) }
            }

            requestView.setOnClickListener { fetchUserDetails(senderId,imageString) }
            requestContainer.addView(requestView)
        }
    }

    private fun fetchUserDetails(userId: String, profileBitmap: String) {
        userRef.child(userId).child("Details").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = HashMap<String, String>()  // Explicitly use HashMap

                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = when (val data = child.value) {
                        is String -> data  // If it's a String, use it directly
                        is List<*> -> data.joinToString(", ")  // Convert list to a comma-separated string
                        else -> "N/A"  // Default value for unexpected types
                    }
                    userData[key] = value
                }

                userData["Image"] = profileBitmap  // Adding the image separately

                val bottomSheet = UserDetailsBottomSheet.newInstance(userData)
                bottomSheet.show(parentFragmentManager, "UserDetailsBottomSheet")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch user details: ${error.message}")
            }
        })
    }




    private fun updateRequestStatus(requestId: String, senderId: String, acceptButton: MaterialCardView, rejectButton: MaterialCardView, statusText: TextView) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        requestRef.child(requestId).child("status").setValue("accepted").addOnSuccessListener {
            val friendRef = FirebaseDatabase.getInstance().reference.child("Users")
            friendRef.child(currentUserId).child("Friends").child(senderId).setValue(true)
            friendRef.child(senderId).child("Friends").child(currentUserId).setValue(true)
            acceptButton.visibility = View.GONE
            rejectButton.visibility = View.GONE
            statusText.text = "Friend"
            statusText.visibility = View.VISIBLE
        }
    }

    private fun rejectFriendRequest(requestId: String, acceptButton: MaterialCardView, rejectButton: MaterialCardView, statusText: TextView) {
        requestRef.child(requestId).removeValue().addOnSuccessListener {
            acceptButton.visibility = View.GONE
            rejectButton.visibility = View.GONE
            statusText.text = "Rejected"
            statusText.visibility = View.VISIBLE
        }
    }
}
