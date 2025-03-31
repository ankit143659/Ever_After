package com.example.ever_after

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

class Chat : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()
    private lateinit var dbRef: DatabaseReference
    private lateinit var messagesRef: DatabaseReference
    private var currentUserId: String? = null
    private lateinit var receiverImageView: CircleImageView


    private lateinit var tvUsername: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnBack: ImageView
    private var isChatOpen = true


    private var receiverId: String? = null
    private var receiverName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Retrieve current user ID from SharedPreferences
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Chat", "User not logged in")
            finish() // Close activity if user is not authenticated
            return
        }

        tvUsername = findViewById(R.id.tvUsername)
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)

        receiverImageView = findViewById(R.id.profilePic) // 🔥 Make sure this ID is in XML
        val receiverImagePath = intent.getStringExtra("receiverImagePath")

        Log.d("ChatActivity", "Received Image Path: $receiverImagePath") // ✅ Debug ke liye

        if (!receiverImagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(receiverImagePath) // File se Bitmap load kar
            if (bitmap != null) {
                receiverImageView.setImageBitmap(bitmap) // ✅ Agar image mili, to set karo
            } else {
                Log.e("ChatActivity", "Bitmap loading failed!") // ✅ Agar bitmap null ho to log error
                receiverImageView.setImageResource(R.drawable.tony) // Default image
            }
        } else {
            Log.e("ChatActivity", "Image Path is NULL or EMPTY!") // ✅ Agar path null hai to error log karo
            receiverImageView.setImageResource(R.drawable.tony)
        }


        // Get receiver data from Intent
        receiverId = intent.getStringExtra("receiverId")
        receiverName = intent.getStringExtra("receiverName")
        tvUsername.text = receiverName ?: "User"

        // Firebase reference for messages
        dbRef = FirebaseDatabase.getInstance().reference
        messagesRef = dbRef.child("chats")

        // Setup RecyclerView with optimized settings
        chatAdapter = ChatAdapter(messageList, currentUserId!!){ message ->
            showDeleteMessageDialog(message)  // Call function to show delete dialog
        }

        rvMessages.adapter = chatAdapter
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true  // Ensures new messages appear at the bottom
        }

        // Load previous chat history
        loadChatHistory()

        // Send message on button click
        btnSend.setOnClickListener {
            sendMessage()
        }

        // Scroll to bottom when clicking EditText
        etMessage.setOnClickListener {
            scrollToBottom()
        }

        // Back button action
        btnBack.setOnClickListener { finish() }

        // Scroll to bottom when keyboard opens
        rvMessages.viewTreeObserver.addOnGlobalLayoutListener {
            scrollToBottom()
        }


    }

    private fun loadChatHistory() {
        if (receiverId.isNullOrEmpty()) return

        val chatId = getChatId(currentUserId!!, receiverId!!)

        messagesRef.child(chatId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messageList.add(message)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    scrollToBottom()

                    // ✅ Only mark as seen if user is in chat and scrolled to bottom
                    if (isChatOpen && message.receiverId == currentUserId && !message.seen) {
                        rvMessages.postDelayed({
                            val layoutManager = rvMessages.layoutManager as LinearLayoutManager
                            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                            if (lastVisibleItem == messageList.size - 1) {
                                snapshot.ref.child("seen").setValue(true)
                            }
                        }, 500) // Small delay to ensure UI updates
                    }
                }


            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedMessage = snapshot.getValue(Message::class.java)
                if (updatedMessage != null) {
                    val index = messageList.indexOfFirst { it.timestamp == updatedMessage.timestamp }
                    if (index != -1) {
                        messageList[index] = updatedMessage
                        chatAdapter.notifyItemChanged(index)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedMessage = snapshot.getValue(Message::class.java)
                if (removedMessage != null) {
                    val index = messageList.indexOfFirst { it.timestamp == removedMessage.timestamp }
                    if (index != -1) {
                        messageList.removeAt(index)
                        chatAdapter.notifyItemRemoved(index)
                        chatAdapter.notifyItemRangeChanged(index, messageList.size)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("Chat", "Failed to load messages", error.toException())
            }
        })
    }



    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isEmpty() || receiverId.isNullOrEmpty()) return

        val chatId = getChatId(currentUserId!!, receiverId!!)
        val messageId = messagesRef.child(chatId).push().key ?: return

        val message = Message(currentUserId!!, receiverId!!, messageText, System.currentTimeMillis())

        messagesRef.child(chatId).child(messageId).setValue(message)
            .addOnSuccessListener {
                etMessage.text.clear()
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                Log.e("Chat", "Failed to send message", e)
            }
    }

    private fun showDeleteMessageDialog(message: Message) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_message, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Find the views inside the custom dialog layout
        val tvDeleteForEveryone = dialogView.findViewById<TextView>(R.id.tvDeleteForEveryone)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)

        // Handle delete for everyone
        tvDeleteForEveryone.setOnClickListener {
            deleteMessage(message)  // Call delete function
            alertDialog.dismiss()
        }

        // Handle cancel
        tvCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    override fun onResume() {
        super.onResume()
        isChatOpen = true

    }

    override fun onPause() {
        super.onPause()
        isChatOpen = false
    }

    private fun deleteMessage(message: Message) {
        val chatId = getChatId(currentUserId!!, receiverId!!)

        messagesRef.child(chatId).orderByChild("message").equalTo(message.message)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (messageSnapshot in snapshot.children) {
                        messageSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                // Remove message from the list
                                val index = messageList.indexOfFirst { it.message == message.message }
                                if (index != -1) {
                                    messageList.removeAt(index)  // Remove item from list
                                    chatAdapter.notifyItemRemoved(index)  // Notify RecyclerView
                                    chatAdapter.notifyItemRangeChanged(index, messageList.size)  // Prevent index issues
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Chat", "Failed to delete message", e)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Chat", "Delete cancelled", error.toException())
                }
            })
    }


    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }


    private fun scrollToBottom() {
        rvMessages.postDelayed({
            if (messageList.isNotEmpty()) {
                rvMessages.smoothScrollToPosition(messageList.size - 1)
            }
        }, 100)  // Delay to ensure UI update
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error decoding Base64", e)
            null
        }
    }

}
