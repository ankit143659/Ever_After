package com.example.ever_after


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class UserListActivity : AppCompatActivity() {

    private lateinit var userAdapter: UserAdapter
    private lateinit var rvChatList: RecyclerView
    private val userList = mutableListOf<AppUser>()
    private lateinit var dbRef: DatabaseReference
    private var currentUserId: String? = null
    private lateinit var userImageView: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        userImageView = findViewById(R.id.btnProfile)
        // Retrieve logged-in user's ID
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Log.e("AuthError", "User not logged in!")
            finish() // Close activity if no user is logged in
            return
        }
        dbRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId!!).child("Images").child("Image1")

        // Fetch and display the image
        fetchAndDisplayUserImage()


        rvChatList = findViewById(R.id.rvChatList)
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.addItemDecoration(SpacingItemDecoration(30))



        // Open chat when user is clicked
        userAdapter = UserAdapter(userList) { user ->
            val compressedImage = compressBase64Image(user.profileImage)
            val intent = Intent(this, Chat::class.java).apply {
                putExtra("senderId", currentUserId)  // Pass sender ID
                putExtra("receiverId", user.id)     // Pass receiver ID
                putExtra("receiverName", user.name)
                putExtra("receiverImage", compressedImage)
            }
            startActivity(intent)
        }
        rvChatList.adapter = userAdapter

        fetchUsersFromRealtimeDatabase()

    }

    private fun fetchAndDisplayUserImage() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val base64Image = snapshot.getValue(String::class.java)
                if (!base64Image.isNullOrEmpty()) {
                    val bitmap = decodeBase64ToBitmap(base64Image)
                    if (bitmap != null) {
                        userImageView.setImageBitmap(bitmap) // Set image to ImageView
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserListActivity", "Failed to load image", error.toException())
            }
        })
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("UserListActivity", "Error decoding Base64", e)
            null
        }
    }

    private fun fetchUsersFromRealtimeDatabase() {
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<AppUser>()
                val usersToFetch = mutableListOf<AppUser>()

                for (userSnapshot in snapshot.children) {
                    val id = userSnapshot.key ?: ""
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                    val base64Image = userSnapshot.child("Images").child("Image1").getValue(String::class.java) ?: ""

                    if (id.isNotEmpty() && id != currentUserId) {
                        val user = AppUser(id, name, "", base64Image,0)
                        usersToFetch.add(user)
                    }
                }

                if (usersToFetch.isNotEmpty()) {
                    fetchLastMessages(usersToFetch) { updatedUsers ->
                        userAdapter.updateUserList(updatedUsers)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching users", error.toException())
            }
        })
    }
    private fun fetchLastMessages(users: List<AppUser>, callback: (List<AppUser>) -> Unit) {
        val updatedUsers = mutableListOf<AppUser>()
        val usersProcessed = mutableSetOf<String>()

        for (user in users) {
            val chatId = getChatId(currentUserId!!, user.id)
            val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)

            chatRef.orderByChild("timestamp").limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            val lastMessage = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                            val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0

                            user.lastMessage = lastMessage
                            user.timestamp = timestamp
                        }

                        if (!usersProcessed.contains(user.id)) {
                            updatedUsers.add(user)
                            usersProcessed.add(user.id)
                        }

                        // Update UI with real-time data
                        callback(updatedUsers.sortedByDescending { it.timestamp })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error fetching last message", error.toException())
                    }
                })
        }
    }


    fun compressBase64Image(base64String: String, maxSizeKB: Int = 100): String {
        try {
            // Decode base64 to Bitmap
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            // Compress the image
            val outputStream = ByteArrayOutputStream()
            var quality = 100 // Start with full quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            // Reduce quality until under maxSizeKB
            while (outputStream.toByteArray().size / 1024 > maxSizeKB && quality > 10) {
                outputStream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            // Convert back to base64
            val compressedBytes = outputStream.toByteArray()
            return Base64.encodeToString(compressedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return base64String // Return original if compression fails
        }
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }

}

