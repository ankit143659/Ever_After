package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton

class detail_2 : Fragment() {
    private lateinit var radioWoman: RadioButton
    private lateinit var radioMan: RadioButton
    private lateinit var radioOther: RadioButton


    private lateinit var optionWoman: LinearLayout
    private lateinit var optionMan: LinearLayout
    private lateinit var optionOther: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detail_2, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioWoman = view.findViewById(R.id.radioWoman)
        radioMan = view.findViewById(R.id.radioMan)
        radioOther = view.findViewById(R.id.radioother)


        optionWoman = view.findViewById(R.id.optionWoman)
        optionMan = view.findViewById(R.id.optionMan)
        optionOther = view.findViewById(R.id.optionother)


        val radioButtons = listOf(radioWoman, radioMan,radioOther)
        val layouts = listOf(optionWoman, optionMan,optionOther)

        for (i in radioButtons.indices) {
            radioButtons[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                }
            }
            layouts[i].setOnClickListener {
                for (j in radioButtons.indices) {
                    val isSelected = (i == j)
                    radioButtons[j].isChecked = isSelected
                    layouts[j].setBackgroundResource(
                        if (isSelected) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg
                    )
                }
            }
        }
    }

}