package com.example.ever_after

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class detail_7 : Fragment() {

    private lateinit var RecyclerView : RecyclerView
    private lateinit var viewModel: dataViewModel

    private val interests = listOf(
        "✏️ Crafts", "🎨 Art", "🎵 R&B", "💃 Dancing", "🏕️ Camping",
        "💛 Feminism", "🎸 Country", "🍰 Baking", "🐱 Cats", "🐶 Dogs"
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(dataViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_detail_7, container, false)
        RecyclerView = view.findViewById(R.id.recyclerView)


        RecyclerView.layoutManager = GridLayoutManager(context,2)
        val adapter = InterestsAdapter(interests,5,viewModel,requireActivity(),"Interest")
        RecyclerView.adapter = adapter


        return view
    }


}