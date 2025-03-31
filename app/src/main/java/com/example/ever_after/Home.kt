
package com.example.ever_after
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import kotlin.math.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentUserLat: Double = 0.0
    private var currentUserLon: Double = 0.0
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // ✅ Check Permission & Fetch Location
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
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
                            getCurrentLocation()
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

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentUserLat = location.latitude
                currentUserLon = location.longitude
                Log.d("Location", "Current Location: $currentUserLat, $currentUserLon")


            } else {
                Log.e("Location", "Failed to get location")
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
//                val currentLat =snapshot.child(currentUserId).child("latitude").value.toString().toDoubleOrNull()
//                val currentLon = snapshot.child(currentUserId).child("longitude").value.toString().toDoubleOrNull()
                val currentUserInterestForKids = currentUserDetails.child("interestForKids").value.toString()
                val currentUserValues = currentUserDetails.child("Value").value.toString()
                if (currentUserLat == null || currentUserLon == null) {
                    Log.e("LocationError", "Current user location not found!")
                    return
                }

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
                    val userLat =userSnapshot.child("latitude").value.toString().toDoubleOrNull()
                    val userLon = userSnapshot.child("longitude").value.toString().toDoubleOrNull()

                    if (userLat == null || userLon == null) continue // Skip if location missing

                    // ✅ Distance Calculate karo
                    val distance = calculateDistance(currentUserLat, currentUserLon, userLat, userLon)


                    // Filter out users jinka gender current user's MeetingPerson se match nahi karta
                    if (userGender != currentMeetingPerson) continue

                    val userImages = userSnapshot.child("Images")
                    val user = userDetails.getValue(UserModel::class.java)


                    if (user != null ) {
                        user.userId = userId
                        user.distance=distance

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
                        if (matchCount >= 2 && matchPercentage >= 10 && distance <= 50.0) {
                            user.matchPercentage = matchPercentage.toString() // Save Match Percentage in Model
                            userModelList.add(user)
                        }
                    }
                }

                // ✅ Sort by Highest Match Percentage
                userModelList.sortBy { it.distance }
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
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Radius of Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c // Distance in km

        return String.format("%.1f", distance).toDouble() // ✅ Round to 1 decimal place
    }


}
