package com.example.ever_after

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private var userList: MutableList<AppUser>,
    private val onUserClick: (AppUser) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        private val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        private val imgUser: CircleImageView = view.findViewById(R.id.profileImage)

        fun bind(user: AppUser) {
            tvUserName.text = user.name
            tvLastMessage.text = user.lastMessage.ifEmpty { "No messages yet" }

            val bitmap = decodeBase64ToBitmap(user.profileImage)
            if (bitmap != null) {
                imgUser.setImageBitmap(bitmap)
            } else {
                imgUser.setImageResource(R.drawable.baseline_person_24) // Default image
            }
            itemView.setOnClickListener { onUserClick(user) }  // 👈 Handle click event
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])


    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
    override fun getItemCount(): Int = userList.size

    fun updateUserList(newList: List<AppUser>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}
