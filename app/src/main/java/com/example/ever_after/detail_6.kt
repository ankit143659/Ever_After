package com.example.ever_after

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class detail_6 : Fragment() {


    private val viewModel: dataViewModel by activityViewModels()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SoonBlockedPrivateApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detail_6, container, false)

        val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Current User ka UID
            database = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Details")
        } else {
            database = FirebaseDatabase.getInstance().getReference("Users").child("Unknown")
        }

        numberPicker.minValue=100
        numberPicker.maxValue=200

        numberPicker.setFormatter { "$it cm" }

        setNumberPickerTextColor(numberPicker,Color.WHITE)
        numberPicker.wrapSelectorWheel = true  // Enables infinite scrolling
        numberPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS


        try {
            val selectorWheelPaintField = numberPicker.javaClass.getDeclaredField("mSelectionDividerHeight")
            selectorWheelPaintField.isAccessible = true
            selectorWheelPaintField.set(numberPicker, 0)  // Remove fading effect
        } catch (e: Exception) {
            e.printStackTrace()
        }

        numberPicker.setOnValueChangedListener{_,_,newVal->
            val text = newVal
            viewModel.updateHeight(text.toString())
            database.child("Height").setValue(text.toString())
        }

        return view
    }

    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        try {
            val count = numberPicker.childCount
            for (i in 0 until count) {
                val child = numberPicker.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(color)
                    child.textSize = 22f
                    child.setTypeface(null, Typeface.BOLD)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}