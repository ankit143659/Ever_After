package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class detail_8 : Fragment() {

    private lateinit var RecyclerView : RecyclerView

    private val interests = listOf(
        "Ambition","Confidence","Empathy","Generosity","Humor","Kindness","Openness","Optimism","Playfulness","Sassiness","Leadership","Curiosity"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_8, container, false)

        RecyclerView = view.findViewById(R.id.recyclerView)

        RecyclerView.layoutManager=GridLayoutManager(context,2)

        val adapter = InterestsAdapter(interests,5)
        RecyclerView.adapter = adapter
        return view
    }


}