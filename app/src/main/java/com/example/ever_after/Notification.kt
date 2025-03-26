package com.example.ever_after

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        requestContainer = view.findViewById(R.id.requestContainer)

        listenForNewRequests()
        return view
    }

    private fun listenForNewRequests() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        requestRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Requests")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        requestRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return
                val status = snapshot.child("status").getValue(String::class.java)
                if (status == "accepted") return
                fetchSenderName(senderId, snapshot.key!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchSenderName(senderId: String, requestId: String) {
        userRef.child(senderId).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val senderName = snapshot.getValue(String::class.java)
                    senderName?.let {
                        addRequestUI(it, senderId, requestId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addRequestUI(senderName: String, senderId: String, requestId: String) {
        if (!isAdded || activity == null) return  // 🔥 Fragment attached है या नहीं check करो

        requireActivity().runOnUiThread {
            val requestView = LayoutInflater.from(requireContext())
                .inflate(R.layout.request_design, requestContainer, false)

            val username = requestView.findViewById<TextView>(R.id.username)
            val acceptButton = requestView.findViewById<MaterialCardView>(R.id.btn_accept)
            val rejectButton = requestView.findViewById<MaterialCardView>(R.id.btn_reject)

            username.text = senderName

            val senderRef = FirebaseDatabase.getInstance().reference.child("Users").child(senderId)
                .child("Requests")

            acceptButton.setOnClickListener {
                requestRef.child(requestId).child("status").setValue("accepted")
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful) {
                            senderRef.child(requestId).child("status").setValue("accepted")
                                .addOnCompleteListener { task2 ->
                                    if (task2.isSuccessful) {
                                        requestContainer.removeView(requestView)
                                    }
                                }
                        }
                    }
            }

            rejectButton.setOnClickListener {
                requestRef.child(requestId).removeValue().addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        senderRef.child(requestId).removeValue().addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                requestContainer.removeView(requestView)
                            }
                        }
                    }
                }
            }

            requestContainer.addView(requestView)
        }
    }
}
