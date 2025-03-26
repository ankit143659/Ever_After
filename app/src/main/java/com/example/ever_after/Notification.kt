package com.example.ever_after

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        requestContainer = view.findViewById(R.id.requestContainer)

        // Listen to the receiver's Requests node only.
        // (Make sure that when a request is sent, it is written only under the receiver's Requests node.)
        listenForRequests()
        return view
    }

    private fun listenForRequests() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to current user's Requests and Friends nodes.
        requestRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(currentUserId).child("Requests")
        val friendRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(currentUserId).child("Friends")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        // First, fetch the current user's Friends list.
        friendRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(friendSnapshot: DataSnapshot) {
                // Build a set of friend IDs for quick lookup.
                val friendIds = mutableSetOf<String>()
                for (friend in friendSnapshot.children) {
                    friend.key?.let { friendIds.add(it) }
                }

                // Now listen to the Requests node.
                requestRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(requestSnapshot: DataSnapshot) {
                        requestContainer.removeAllViews() // Clear UI before updating

                        for (request in requestSnapshot.children) {
                            // Get the sender's ID from the request.
                            val senderId = request.child("senderId").getValue(String::class.java) ?: continue
                            // Check if the sender's ID is already in the Friends list.
                            val isAccepted = friendIds.contains(senderId)

                            // Call fetchSenderName to build the UI.
                            fetchSenderName(senderId, request.key!!, isAccepted)
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

    private fun fetchSenderName(senderId: String, requestId: String, isAccepted: Boolean) {
        userRef.child(senderId).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(String::class.java)?.let { senderName ->
                        addRequestUI(senderName, senderId, requestId, isAccepted)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch sender name: ${error.message}")
                }
            })
    }

    private fun addRequestUI(senderName: String, senderId: String, requestId: String, isAccepted: Boolean) {
        if (!isAdded || activity == null) return

        requireActivity().runOnUiThread {
            val requestView = LayoutInflater.from(requireContext())
                .inflate(R.layout.request_design, requestContainer, false)

            val usernameText = requestView.findViewById<TextView>(R.id.username)
            val acceptButton = requestView.findViewById<MaterialCardView>(R.id.btn_accept)
            val rejectButton = requestView.findViewById<MaterialCardView>(R.id.btn_reject)
            val statusText = requestView.findViewById<TextView>(R.id.status)

            usernameText.text = senderName

            if (isAccepted) {
                // If the sender's ID is in the Friends list, show accepted state.
                acceptButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
                statusText.text = "Friend"
                statusText.visibility = View.VISIBLE
            } else {
                // Otherwise, show Accept/Reject buttons.
                acceptButton.visibility = View.VISIBLE
                rejectButton.visibility = View.VISIBLE
                statusText.visibility = View.GONE

                acceptButton.setOnClickListener {
                    updateRequestStatus(requestId, senderId, acceptButton, rejectButton, statusText)
                }

                rejectButton.setOnClickListener {
                    rejectFriendRequest(requestId, acceptButton, rejectButton, statusText)
                }
            }

            requestContainer.addView(requestView)
        }
    }

    private fun updateRequestStatus(
        requestId: String,
        senderId: String,
        acceptButton: MaterialCardView,
        rejectButton: MaterialCardView,
        statusText: TextView
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Update the request status in the receiver's Requests node.
        requestRef.child(requestId).child("status").setValue("accepted")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 2. Add to both users' Friends node.
                    val currentUserFriendsRef = FirebaseDatabase.getInstance().reference
                        .child("Users").child(currentUserId).child("Friends")
                    val senderFriendsRef = FirebaseDatabase.getInstance().reference
                        .child("Users").child(senderId).child("Friends")

                    currentUserFriendsRef.child(senderId).setValue(true)
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                senderFriendsRef.child(currentUserId).setValue(true)
                                    .addOnCompleteListener { task3 ->
                                        if (task3.isSuccessful) {
                                            acceptButton.visibility = View.GONE
                                            rejectButton.visibility = View.GONE
                                            statusText.text = "Friend"
                                            statusText.visibility = View.VISIBLE
                                        }
                                    }
                            }
                        }
                }
            }
    }

    // Remove the request from the receiver's Requests node on reject.
    private fun rejectFriendRequest(
        requestId: String,
        acceptButton: MaterialCardView,
        rejectButton: MaterialCardView,
        statusText: TextView
    ) {
        requestRef.child(requestId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                acceptButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
                statusText.text = "Rejected"
                statusText.visibility = View.VISIBLE
            }
        }
    }
}
