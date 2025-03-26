package com.example.ever_after

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerViewContainer: ShimmerFrameLayout

    private lateinit var adapter: UserHomeAdapter
    private lateinit var userModelList: MutableList<UserModel>
    private lateinit var database: DatabaseReference
    private lateinit var currentUserInterests: String // Interest field as String
    private lateinit var currentUserGender: String // currentUserGender field as String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ChatButton:FloatingActionButton=view.findViewById(R.id.fab_chat)
        shimmerViewContainer =view.findViewById(R.id.shimmer_view_container)
        recyclerView = view.findViewById(R.id.recycler_view)
        ChatButton.setOnClickListener {
            val intent = Intent(requireContext(), UserListActivity::class.java)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = null


        userModelList = mutableListOf()
        adapter = UserHomeAdapter(userModelList,requireContext())
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
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModelList.clear()

                // ✅ Current User Data Fetch
                val currentUserDetails = snapshot.child(currentUserId).child("Details")
                val currentMeetingPerson = currentUserDetails.child("MeetingPerson").value.toString().trim()
                val currentUserInterests = currentUserDetails.child("Hope").child("Interest").value.toString()
                val currentUserDrinking = currentUserDetails.child("DrinkingStatus").value.toString()
                val currentUserSmoking = currentUserDetails.child("SmokingStatus").value.toString()
                val currentUserReligion = currentUserDetails.child("Religion").value.toString()
                val currentUserHaveKids = currentUserDetails.child("haveKids").value.toString()
                val currentUserInterestForKids = currentUserDetails.child("interestForKids").value.toString()
                val currentUserValues = currentUserDetails.child("Value").value.toString()

                // ✅ Fetch Disliked Users Directly
                val dislikedUsers = mutableSetOf<String>()
                for (dislikedUserSnapshot in snapshot.child(currentUserId).child("DislikedUsers").children) {
                    dislikedUsers.add(dislikedUserSnapshot.key.toString())
                }
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key.toString()

                    // ✅ Ignore Current User & Disliked Users
                    if (userId == currentUserId || dislikedUsers.contains(userId)) continue

                    val userDetails = userSnapshot.child("Details")
                    val userGender = userDetails.child("Gender").value.toString().trim()

                    // Filter out users jinka gender current user's MeetingPerson se match nahi karta
                    if (userGender != currentMeetingPerson) continue

                    val userImages = userSnapshot.child("Images")
                    val user = userDetails.getValue(UserModel::class.java)


                    if (user != null ) {
                        user.userId = userId

                        // ✅ Fetch User's Image
                        val imageBase64 = userImages.child("Image1").value?.toString()
                        user.Image1 = imageBase64.toString()

                        // ✅ Fetch User's Gender
                        val userGender = userDetails.child("Gender").value.toString().trim()

                        // ✅ Fetch Other User's Data
                        val userInterests = userDetails.child("Hope").child("Interest").value.toString()
                        val userDrinking = userDetails.child("DrinkingStatus").value.toString()
                        val userSmoking = userDetails.child("SmokingStatus").value.toString()
                        val userReligion = userDetails.child("Religion").value.toString()
                        val userHaveKids = userDetails.child("haveKids").value.toString()
                        val userInterestForKids = userDetails.child("interestForKids").value.toString()
                        val userValues = userDetails.child("Value").value.toString()

                        // ✅ Check Matching Criteria
                        var matchCount = 0
                        val totalCriteria = 8 // Total Matching Factors

                        if (userGender == currentMeetingPerson) matchCount++
                        if (userDrinking == currentUserDrinking) matchCount++
                        if (userSmoking == currentUserSmoking) matchCount++
                        if (userReligion == currentUserReligion) matchCount++
                        if (userHaveKids == currentUserHaveKids) matchCount++
                        if (userInterestForKids == currentUserInterestForKids) matchCount++
                        if (userValues == currentUserValues) matchCount++

                        // ✅ Interest Matching
                        val commonInterests = userInterests.split(", ").intersect(currentUserInterests.split(", "))
                        if (commonInterests.size >= 1) matchCount++

                        // ✅ Calculate Percentage
                        val matchPercentage: Int = ((matchCount.toDouble() / totalCriteria) * 100).toInt()

                        // ✅ Final Condition: At least 3 Matches & 50% Match Percentage
                        if (matchCount >= 2 && matchPercentage >= 10) {
                            user.matchPercentage = matchPercentage.toString() // Save Match Percentage in Model
                            userModelList.add(user)
                        }
                    }
                }

                // ✅ Sort by Highest Match Percentage
                userModelList.sortByDescending { it.matchPercentage }
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchProfiles", "Error: ${error.message}")
            }
        })
    }

}
