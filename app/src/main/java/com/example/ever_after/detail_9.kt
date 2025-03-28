package com.example.ever_after

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class detail_9 : Fragment() {

    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerView2 : RecyclerView

    private val interest = listOf(
        "yes, I drink", "I drink sometimes","No, I don't drink"
    )

    private val interest2 = listOf(
        "yes, I smoke", "I smoke sometimes","No, I don't smoke"
    )

    private lateinit var viewModel: dataViewModel
    private lateinit var viewModel2: dataModel2



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_9, container, false)

        viewModel = ViewModelProvider(this)[dataViewModel::class.java]
        viewModel2 = ViewModelProvider(this)[dataModel2::class.java]
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView2 = view.findViewById(R.id.recyclerView2)

        recyclerView.layoutManager = GridLayoutManager(context,1)
        recyclerView2.layoutManager = GridLayoutManager(context,1)

        var adapter = InterestsAdapter(interest,1,viewModel,requireActivity(),"DrinkingStatus")
        recyclerView.adapter=adapter

       val  adapter2 = interesetAdapter2(interest2,1,viewModel2,requireActivity(),"SmokingStatus")
        recyclerView2.adapter = adapter2



        return view
    }

}