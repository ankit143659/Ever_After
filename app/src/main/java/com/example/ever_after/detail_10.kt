package com.example.ever_after

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class detail_10 : Fragment() {


    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerView2 : RecyclerView

    private val interest = listOf(
        "Have kids","Don't have kids"
    )

    private lateinit var viewModel: dataViewModel


    private val interest2 = listOf(
        "Don't want kids","Open to kids","Want kids","Not sure"
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_10, container, false)

        viewModel = ViewModelProvider(this).get(dataViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView2 = view.findViewById(R.id.recyclerView2)

        recyclerView.layoutManager = GridLayoutManager(context,1)
        recyclerView2.layoutManager = GridLayoutManager(context,1)

        var adapter = InterestsAdapter(interest,1,viewModel,requireActivity(),"HaveKids")
        recyclerView.adapter=adapter

        adapter = InterestsAdapter(interest2,1,viewModel,requireActivity(),"Kids")
        recyclerView2.adapter = adapter



        return view
    }

}