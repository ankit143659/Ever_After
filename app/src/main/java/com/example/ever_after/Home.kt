package com.example.ever_after

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var adapter: UserAdapter
    private lateinit var userModelList: MutableList<UserModel>
    private lateinit var database: DatabaseReference
    private lateinit var currentUserInterests: String // Interest field as String

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
        adapter = UserAdapter(userModelList)
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance().reference.child("Users")

        if (userId != null) {
            // **Fetch Current User's Interests**
            database.child(userId).child("Details").child("Interest").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        currentUserInterests = snapshot.value.toString()
                        Log.d("FirebaseUser", "Fetched Current User Interests: $currentUserInterests") // ✅ Debugging
                        fetchMatchingProfiles(userId)
                    } else {
                        Log.e("FirebaseUser", "Interest field not found for user: $userId")
                    }
                }.addOnFailureListener {
                    Log.e("FirebaseUser", "Error fetching Interest: ${it.message}")
                }

        }
    }

    private fun fetchMatchingProfiles(currentUserId: String) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModelList.clear()
                for (userSnapshot in snapshot.children) {
                    val userDetails = userSnapshot.child("Details")
                    val userImages = userSnapshot.child("Images") // ✅ Images node fetch karo
                    val user = userDetails.getValue(UserModel::class.java)

                    if (user != null && user.userId != currentUserId) {
                        // ✅ Base64 Image Fetch Karna
                        val imageBase64 = userImages.child("Image1").value?.toString()
                        user.Image1 = imageBase64.toString() // UserModel me assign karna
                        Log.d("image", "Aryan $imageBase64")

                        val userInterests = userDetails.child("Interest").value.toString()
                        val commonInterests = userInterests.split(" ").intersect(currentUserInterests.split(" "))

                        if (commonInterests.size >= 3) {
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
