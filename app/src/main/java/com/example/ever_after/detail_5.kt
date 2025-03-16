package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton


class detail_5 : Fragment() {

    private lateinit var r1: RadioButton
    private lateinit var r2: RadioButton
    private lateinit var r3: RadioButton
    private lateinit var r4: RadioButton
    private lateinit var r5: RadioButton
    private lateinit var r6: RadioButton



    private lateinit var l1: LinearLayout
    private lateinit var l2: LinearLayout
    private lateinit var l3: LinearLayout
    private lateinit var l4: LinearLayout
    private lateinit var l5: LinearLayout
    private lateinit var l6: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_5, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        l1 = view.findViewById(R.id.l1)
        l2 = view.findViewById(R.id.l2)
        l3 = view.findViewById(R.id.l3)
        l4 = view.findViewById(R.id.l4)
        l5 = view.findViewById(R.id.l5)
        l6 = view.findViewById(R.id.l6)

        r1 = view.findViewById(R.id.r1)
        r2 = view.findViewById(R.id.r2)
        r3 = view.findViewById(R.id.r3)
        r4 = view.findViewById(R.id.r4)
        r5 = view.findViewById(R.id.r5)
        r6 = view.findViewById(R.id.r6)


        val radioButtons = listOf(r1,r2,r3,r4,r5,r6)
        val layouts = listOf(l1,l2,l3,l4,l5,l6)

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