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


class detail_5 : Fragment() {

    private lateinit var r1: RadioButton
    private lateinit var r2: RadioButton
    private lateinit var r3: RadioButton
    private lateinit var r4: RadioButton
    private lateinit var r5: RadioButton
    private lateinit var r6: RadioButton

    private val selectedOptions = mutableListOf<String>()


    private lateinit var l1: LinearLayout
    private lateinit var l2: LinearLayout
    private lateinit var l3: LinearLayout
    private lateinit var l4: LinearLayout
    private lateinit var l5: LinearLayout
    private lateinit var l6: LinearLayout

    private lateinit var text1: TextView
    private lateinit var text2: TextView
    private lateinit var text3: TextView
    private lateinit var text4: TextView
    private lateinit var text5: TextView
    private lateinit var text6: TextView


    private lateinit var database: DatabaseReference
    private val viewModel: dataViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth


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

        text1 = view.findViewById(R.id.text1)
        text2 = view.findViewById(R.id.text2)
        text3 = view.findViewById(R.id.text3)
        text4 = view.findViewById(R.id.text4)
        text5 = view.findViewById(R.id.text5)
        text6 = view.findViewById(R.id.text6)


        val radioButtons = listOf(r1, r2, r3, r4, r5, r6)
        val layouts = listOf(l1, l2, l3, l4, l5, l6)
        val textViews = listOf(text1, text2, text3, text4, text5, text6)




        for (i in radioButtons.indices) {
            radioButtons[i].setOnClickListener {
                handleSelection(i, textViews[i].text.toString())
            }
            layouts[i].setOnClickListener {
                handleSelection(i, textViews[i].text.toString())
            }

        }
    }

    private fun handleSelection(index: Int, text: String) {
        if (selectedOptions.contains(text)) {
            selectedOptions.remove(text) // If already selected, remove
        } else {
            if (selectedOptions.size >= 2) {
                val removedText = selectedOptions.removeAt(0) // Remove first selected
                val removedIndex = getIndexFromText(removedText)

                r1.isChecked = removedIndex == 0
                r2.isChecked = removedIndex == 1
                r3.isChecked = removedIndex == 2
                r4.isChecked = removedIndex == 3
                r5.isChecked = removedIndex == 4
                r6.isChecked = removedIndex == 5

                l1.setBackgroundResource(if (removedIndex == 0) R.drawable.radio_unselected_bg else R.drawable.radio_selected_bg)
                l2.setBackgroundResource(if (removedIndex == 1) R.drawable.radio_unselected_bg else R.drawable.radio_selected_bg)
                l3.setBackgroundResource(if (removedIndex == 2) R.drawable.radio_unselected_bg else R.drawable.radio_selected_bg)
                l4.setBackgroundResource(if (removedIndex == 2) R.drawable.radio_unselected_bg else R.drawable.radio_selected_bg)
                l5.setBackgroundResource(if (removedIndex == 2) R.drawable.radio_unselected_bg else R.drawable.radio_selected_bg)
                l6.setBackgroundResource(if (removedIndex == 2) R.drawable.radio_unselected_bg else R.drawable.radio_selected_bg)
            }
            selectedOptions.add(text)
        }

        updateUI()
        viewModel.updateHope(selectedOptions)  // Store in ViewModel
    }


    private fun updateUI() {

        r1.isChecked = selectedOptions.contains(text1.text.toString())
        r2.isChecked = selectedOptions.contains(text2.text.toString())
        r3.isChecked = selectedOptions.contains(text3.text.toString())
        r4.isChecked = selectedOptions.contains(text4.text.toString())
        r5.isChecked = selectedOptions.contains(text5.text.toString())
        r6.isChecked = selectedOptions.contains(text6.text.toString())

        l1.setBackgroundResource(if (selectedOptions.contains(text1.text.toString())) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg)
        l2.setBackgroundResource(if (selectedOptions.contains(text2.text.toString())) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg)
        l3.setBackgroundResource(if (selectedOptions.contains(text3.text.toString())) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg)
        l4.setBackgroundResource(if (selectedOptions.contains(text4.text.toString())) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg)
        l5.setBackgroundResource(if (selectedOptions.contains(text5.text.toString())) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg)
        l6.setBackgroundResource(if (selectedOptions.contains(text6.text.toString())) R.drawable.radio_selected_bg else R.drawable.radio_unselected_bg)
    }

    private fun getIndexFromText(text: String): Int {
        return when (text) {
            text1.text.toString() -> 0
            text2.text.toString() -> 1
            text3.text.toString() -> 2
            text4.text.toString() -> 3
            text5.text.toString() -> 4
            text6.text.toString() -> 5
            else -> -1
        }
    }
}


