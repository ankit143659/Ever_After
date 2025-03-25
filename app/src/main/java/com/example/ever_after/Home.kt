package com.example.ever_after

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserHomeAdapter
    private lateinit var userModelList: MutableList<UserModel>
    private lateinit var database: DatabaseReference
    private lateinit var currentUserInterests: String // Interest field as String
    private lateinit var currentUserGender: String // Interest field as String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        userModelList = mutableListOf()
        adapter = UserHomeAdapter(userModelList)
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance().reference.child("Users")

        if (userId != null) {
            // **Fetch Current User's Interests & Gender**
            database.child(userId).child("Details").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        currentUserInterests = snapshot.child("Hope").child("Interest").value.toString().trim()
                        currentUserGender = snapshot.child("Gender").value.toString().trim()

                        if (currentUserInterests.isNotEmpty() && currentUserGender.isNotEmpty()) {
                            Log.d("FirebaseUser", "Fetched Interests: $currentUserInterests, Gender: $currentUserGender")
                            fetchMatchingProfiles(userId)
                        } else {
                            Log.e("FirebaseUser", "Interest or Gender is missing for user: $userId")
                        }
                    } else {
                        Log.e("FirebaseUser", "Details not found for user: $userId")
                    }
                }.addOnFailureListener {
                    Log.e("FirebaseUser", "Error fetching data: ${it.message}")
                }
        }


    }
    private fun fetchMatchingProfiles(currentUserId: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModelList.clear()
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.child("userId").value?.toString() ?: ""
                    val userDetails = userSnapshot.child("Details")
                    val userImages = userSnapshot.child("Images")
                    val user = userDetails.getValue(UserModel::class.java)

                    if (user != null && user.userId != currentUserId) {
                        user.userId=userId
                        // ✅ Fetch User's Image
                        val imageBase64 = userImages.child("Image1").value?.toString()
                        user.Image1 = imageBase64.toString()

                        // ✅ Fetch User's Gender
                        val userGender = userDetails.child("Gender").value.toString().trim()

                        // ✅ Fetch User's Interests
                        val userInterests = userDetails.child("Hope").child("Interest").value.toString()
                        val commonInterests = userInterests.split(", ").intersect(currentUserInterests.split(", "))

                        // ✅ Gender Match + Minimum 3 Common Interests
                        if (userGender == currentUserGender && commonInterests.size >= 1) {
                            userModelList.add(user)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
    }

}
