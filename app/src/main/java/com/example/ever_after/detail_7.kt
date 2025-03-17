package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class detail_7 : Fragment() {

    private lateinit var RecyclerView : RecyclerView

    private val interests = listOf(
        "✏️ Crafts", "🎨 Art", "🎵 R&B", "💃 Dancing", "🏕️ Camping",
        "💛 Feminism", "🎸 Country", "🍰 Baking", "🐱 Cats", "🐶 Dogs"
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_7, container, false)
        RecyclerView = view.findViewById(R.id.recyclerView)


        RecyclerView.layoutManager = GridLayoutManager(context,2)
        val adapter = InterestsAdapter(interests,5)
        RecyclerView.adapter = adapter
        return view
    }


}