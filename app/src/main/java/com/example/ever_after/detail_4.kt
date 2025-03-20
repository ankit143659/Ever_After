package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class detail_4 : Fragment() {

    private lateinit var radioWoman: RadioButton
    private lateinit var radioMan: RadioButton
    private lateinit var radioOther: RadioButton


    private lateinit var optionWoman: LinearLayout
    private lateinit var optionMan: LinearLayout
    private lateinit var optionOther: LinearLayout

    private lateinit var l1 : TextView
    private lateinit var l2 :  TextView
    private lateinit var l3 :  TextView

    private lateinit var database: DatabaseReference
    private val viewModel: dataViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        database = FirebaseDatabase.getInstance().getReference("Users")

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Current User ka UID
            database = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Details")
        } else {
            database = FirebaseDatabase.getInstance().getReference("Users").child("Unknown")
        }
        return inflater.inflate(R.layout.fragment_detail_4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioWoman = view.findViewById(R.id.radioWoman)
        radioMan= view.findViewById(R.id.radioMan)
        radioOther = view.findViewById(R.id.radioAll)


        optionWoman = view.findViewById(R.id.optionWoman)
        optionMan = view.findViewById(R.id.optionMan)
        optionOther = view.findViewById(R.id.optionAll)

        l1= view.findViewById(R.id.tvWoman)
        l2= view.findViewById(R.id.tvMan)
        l3= view.findViewById(R.id.tvAll)


        val radioButtons = listOf(radioWoman, radioMan,radioOther)
        val layouts = listOf(optionWoman, optionMan,optionOther)
        val layouts2 = listOf(l1,l2,l3)

        for (i in radioButtons.indices) {
            radioButtons[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                    layouts2[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                }
                val text = layouts2[i].text.toString()
                viewModel.genderMeet(text)
                database.child("MeetingPerson").setValue(text)
            }
            layouts[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                    layouts2[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                }
                val text = layouts2[i].text.toString()
                viewModel.genderMeet(text)
                database.child("MeetingPerson").setValue(text)
            }

            layouts2[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts2[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                }
                val text = layouts2[i].text.toString()
                viewModel.genderMeet(text)
                database.child("MeetingPerson").setValue(text)
            }
        }
    }

}