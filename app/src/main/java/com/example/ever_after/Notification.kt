package com.example.ever_after

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
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

        loadAcceptedRequests()
        listenForNewRequests()
        return view
    }

    private fun loadAcceptedRequests() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        requestRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Requests")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        requestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestContainer.removeAllViews() // ✅ Clear previous views

                for (request in snapshot.children) {
                    val senderId = request.child("senderId").getValue(String::class.java) ?: continue
                    val status = request.child("status").getValue(String::class.java)

                    if (status == "accepted") {
                        fetchSenderName(senderId, request.key!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch accepted requests: ${error.message}")
            }
        })
    }


    private fun listenForNewRequests() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        requestRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Requests")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        requestRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return
                val status = snapshot.child("status").getValue(String::class.java)
                val reciverId = snapshot.child("reciverId").getValue(String::class.java)
                if (status != "accepted") {
                    fetchSenderName(senderId, snapshot.key!!)
                      // ✅ New request par notification bhejna
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
    }



    private fun fetchSenderName(senderId: String, requestId: String) {
        userRef.child(senderId).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(String::class.java)?.let {
                        addRequestUI(it, senderId, requestId)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch sender name: ${error.message}")
                }
            })
    }

    private fun addRequestUI(senderName: String, senderId: String, requestId: String) {
        if (!isAdded || activity == null) return

        requireActivity().runOnUiThread {
            val requestView = LayoutInflater.from(requireContext())
                .inflate(R.layout.request_design, requestContainer, false)

            val username = requestView.findViewById<TextView>(R.id.username)
            val acceptButton = requestView.findViewById<MaterialCardView>(R.id.btn_accept)
            val rejectButton = requestView.findViewById<MaterialCardView>(R.id.btn_reject)
            val status = requestView.findViewById<TextView>(R.id.status)

            username.text = senderName

            val senderRef = FirebaseDatabase.getInstance().reference.child("Users")
                .child(senderId).child("Requests").child(requestId)

            // Fetch request status to update UI correctly
            senderRef.child("status").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestStatus = snapshot.getValue(String::class.java)

                    if (requestStatus == "accepted") {
                        acceptButton.visibility = View.GONE
                        rejectButton.visibility = View.GONE
                        status.text = "Accepted"
                        status.visibility = View.VISIBLE
                    } else {
                        acceptButton.visibility = View.VISIBLE
                        rejectButton.visibility = View.VISIBLE
                        status.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch request status: ${error.message}")
                }
            })

            acceptButton.setOnClickListener {
                updateRequestStatus(requestId, senderRef, "accepted", acceptButton, rejectButton, status)
            }

            rejectButton.setOnClickListener {
                removeRequest(requestId, senderRef, acceptButton, rejectButton, status)
            }

            requestContainer.addView(requestView)
        }
    }


    private fun updateRequestStatus(
        requestId: String,
        senderRef: DatabaseReference,
        newStatus: String,
        acceptButton: MaterialCardView,
        rejectButton: MaterialCardView,
        status: TextView
    ) {
        requestRef.child(requestId).child("status").setValue(newStatus)
            .addOnCompleteListener { task1 ->
                if (task1.isSuccessful) {
                    senderRef.child(requestId).child("status").setValue(newStatus)
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                acceptButton.visibility = View.GONE
                                rejectButton.visibility = View.GONE
                                status.text = newStatus.capitalize()
                                status.visibility = View.VISIBLE
                            }
                        }
                }
            }
    }

    private fun removeRequest(
        requestId: String,
        senderRef: DatabaseReference,
        acceptButton: MaterialCardView,
        rejectButton: MaterialCardView,
        status: TextView
    ) {
        requestRef.child(requestId).removeValue().addOnCompleteListener { task1 ->
            if (task1.isSuccessful) {
                senderRef.child(requestId).removeValue().addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        acceptButton.visibility = View.GONE
                        rejectButton.visibility = View.GONE
                        status.text = "Rejected"
                        status.visibility = View.VISIBLE
                    }
                }
            }
        }
    }




}
