package com.example.ever_after

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserHomeAdapter(private val userList: List<UserModel>) :
    RecyclerView.Adapter<UserHomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nameText: TextView = view.findViewById(R.id.profile_name)
        val locationText: TextView = view.findViewById(R.id.profile_location)
        val interestsLayout: LinearLayout = view.findViewById(R.id.interests_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.demo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = "${user.name}, ${user.DOB}"
        holder.locationText.text = user.Communities

        // **Decode Base64 Image**
        if (!user.Image1.isNullOrEmpty()) {
            Log.d("UserAdapter", "Base64 Image Length: ${user.Image1!!.length}")
            holder.profileImage.setImageBitmap(user.Image1?.let { decodeBase64ToBitmap(it) })
        }
        else{
            holder.profileImage.setImageResource(R.drawable.tony)
        }

        // **Dynamically Add Interests**
        holder.interestsLayout.removeAllViews()
        user.Interest.split(" ").forEach { interest ->
            val textView = TextView(holder.itemView.context).apply {
                text = interest
                setPadding(16, 8, 16, 8)
                setBackgroundResource(R.drawable.tag_bg)
                setTextColor(Color.WHITE)
            }
            holder.interestsLayout.addView(textView)
        }
    }

    override fun getItemCount() = userList.size
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
