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

class detail_2 : Fragment() {
    private lateinit var radioWoman: RadioButton
    private lateinit var radioMan: RadioButton
    private lateinit var radioOther: RadioButton


    private lateinit var optionWoman: LinearLayout
    private lateinit var optionMan: LinearLayout
    private lateinit var optionOther: LinearLayout

    private lateinit var woman : TextView
    private lateinit var other : TextView
    private lateinit var man : TextView
    private lateinit var headerText : TextView

    private lateinit var sharePrefrence: SharePrefrence


    private val viewModel: dataViewModel by activityViewModels()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detail_2, container, false)

        sharePrefrence = SharePrefrence(requireContext())

        headerText = view.findViewById(R.id.headerText)
        headerText.setText("${sharePrefrence.getName()} is a great name")
        database = FirebaseDatabase.getInstance().getReference("Users")

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Current User ka UID
            database = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Details")
        } else {
            database = FirebaseDatabase.getInstance().getReference("Users").child("Unknown")
        }



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioWoman = view.findViewById(R.id.radioWoman)
        radioMan = view.findViewById(R.id.radioMan)
        radioOther = view.findViewById(R.id.radioother)

        woman = view.findViewById(R.id.tvWoman)
        man = view.findViewById(R.id.tvMan)
        other = view.findViewById(R.id.tvother)




        optionWoman = view.findViewById(R.id.optionWoman)
        optionMan = view.findViewById(R.id.optionMan)
        optionOther = view.findViewById(R.id.optionother)


        val radioButtons = listOf(radioWoman, radioMan,radioOther)
        val layouts = listOf(optionWoman, optionMan,optionOther)
        val textViews = listOf(woman, man, other)

        for (i in radioButtons.indices) {
            radioButtons[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )

                }
                val text = textViews[i].text.toString()
                viewModel.updateGender(text)
                database.child("Gender").setValue(text)

            }
            layouts[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                }
                val text = textViews[i].text.toString()
                viewModel.updateGender(text)
                database.child("Gender").setValue(text)
            }
        }
    }

}