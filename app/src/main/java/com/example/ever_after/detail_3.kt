package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton

class detail_3 : Fragment() {

    private lateinit var radiodate: RadioButton
    private lateinit var radiobff: RadioButton
    private lateinit var radiobizz: RadioButton


    private lateinit var optionDate: LinearLayout
    private lateinit var optionBff: LinearLayout
    private lateinit var optionBizz: LinearLayout
    private lateinit var l1 : LinearLayout
    private lateinit var l2 : LinearLayout
    private lateinit var l3 : LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_3, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radiodate = view.findViewById(R.id.radioDate)
        radiobff= view.findViewById(R.id.radiobff)
        radiobizz = view.findViewById(R.id.radioBizz)


        optionDate = view.findViewById(R.id.optioDate)
        optionBff = view.findViewById(R.id.optionBff)
        optionBizz = view.findViewById(R.id.optionbizz)

        l1= view.findViewById(R.id.l1)
        l2= view.findViewById(R.id.l2)
        l3= view.findViewById(R.id.l3)


        val radioButtons = listOf(radiodate,radiobff,radiobizz)
        val layouts = listOf(optionDate,optionBff,optionBizz)
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
            }
        }
    }

}