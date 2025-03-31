package com.example.ever_after

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ever_after.databinding.FragmentProfileBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentProfileBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(ARG_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentuser= FirebaseAuth.getInstance().currentUser?.uid

        database = FirebaseDatabase.getInstance().getReference("Users")

        userId?.let {
            fetchUserData(it)
        }
        if (currentuser != null) {
            fetchRequestStatus(currentuser,userId!!)
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.RequestBtn.setOnClickListener {
            userId?.let { it1 ->
                if (currentuser != null) {
                    sendRequest(currentuser, it1)
                }
            }
        }
    }
    private fun sendRequest(
        senderId: String,
        receiverId: String
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
                   requireContext(),
                    "Request Sent",
                    Toast.LENGTH_SHORT
                ).show()


                binding.RequestBtn.text = "Request Sent"
              binding.RequestBtn.isEnabled = false

            }.addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to send request: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Failed to generate request key!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun fetchRequestStatus(senderId: String, receiverId: String) {
        val requestRef = database.child(receiverId).child("Requests")

        requestRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return  // ✅ Fragment Attached Hai Ya Nahi Check Karein

                for (requestSnapshot in snapshot.children) {
                    val requestSenderId = requestSnapshot.child("senderId").getValue(String::class.java)
                    val status = requestSnapshot.child("status").getValue(String::class.java)

                    if (requestSenderId == senderId) {
                        if (!isAdded || _binding == null) return  // ✅ Ensure binding is not null

                        when (status) {
                            "pending" -> {
                                binding?.RequestBtn?.text = "Request Sent"
                                binding?.RequestBtn?.isEnabled = false
                            }
                            "accepted" -> {
                                binding?.RequestBtn?.text = "Friends"
                                binding?.RequestBtn?.isEnabled = false
                            }
                            else -> {
                                binding?.RequestBtn?.text = "Send Request"
                                binding?.RequestBtn?.isEnabled = true
                            }
                        }
                        return
                    }
                }

                if (!isAdded || _binding == null) return  // ✅ Double Check for Safety
                binding?.RequestBtn?.text = "Send Request"
                binding?.RequestBtn?.isEnabled = true
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return  // ✅ Avoid Crash
                Toast.makeText(requireContext(), "Failed to check request status", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchUserData(userId: String) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Ye values direct user ke under hain
                    val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                    val email = snapshot.child("email").getValue(String::class.java) ?: "N/A"
                    val phone = snapshot.child("phone").getValue(String::class.java) ?: "N/A"

                    // "Details" node ke andar wali values
                    val detailsSnapshot = snapshot.child("Details")
                    val dob = detailsSnapshot.child("DOB").getValue(String::class.java) ?: "N/A"
                    val gender = detailsSnapshot.child("Gender").getValue(String::class.java) ?: "N/A"
                    val height = detailsSnapshot.child("Height").getValue(String::class.java) ?: "N/A"
                    val communities = detailsSnapshot.child("Communities").getValue(String::class.java) ?: "N/A"
                    val drinkingStatus = detailsSnapshot.child("DrinkingStatus").getValue(String::class.java) ?: "N/A"
                    val smokingStatus = detailsSnapshot.child("SmokingStatus").getValue(String::class.java) ?: "N/A"
                    val interests = detailsSnapshot.child("Interest").getValue(String::class.java) ?: "N/A"
                    val meetingPerson = detailsSnapshot.child("MeetingPerson").getValue(String::class.java) ?: "N/A"
                    val purpose = detailsSnapshot.child("Purpose").getValue(String::class.java) ?: "N/A"
                    val religion = detailsSnapshot.child("Religion").getValue(String::class.java) ?: "N/A"
                    val values = detailsSnapshot.child("Value").getValue(String::class.java) ?: "N/A"
                    val image=snapshot.child("Images").child("Image1").getValue(String::class.java)
                    binding.profileImageView.setImageBitmap(decodeBase64ToBitmap(image))

                    // UI Update
                    binding.nameTextView.text = name
                    binding.emailTextView.text = "Email: $email"
                    binding.phoneTextView.text = "Phone: $phone"
                    binding.dobTextView.text = "DOB: $dob"
                    binding.genderTextView.text = "Gender: $gender"
                    binding.heightTextView.text = "Height: $height"
                    binding.communitiesTextView.text = "Communities: $communities"
                    binding.drinkingStatusTextView.text = "Drinks: $drinkingStatus"
                    binding.smokingStatusTextView.text = "Smokes: $smokingStatus"
                    binding.interestsTextView.text = "Interests: $interests"
                    binding.meetingPersonTextView.text = "Meeting: $meetingPerson"
                    binding.purposeTextView.text = "Purpose: $purpose"
                    binding.religionTextView.text = "Religion: $religion"
                    binding.valuesTextView.text = "Values: $values"
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.detail.visibility = View.VISIBLE

                } else {
                    Log.e("FirebaseError", "User not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch user data", error.toException())
            }
        })
    }

    fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrEmpty()) {
            Log.e("DecodeError", "Base64 string is null or empty")
            return null
        }

        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("DecodeError", "Error decoding Base64: ${e.message}")
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: String): ProfileBottomSheetFragment {
            val fragment = ProfileBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
