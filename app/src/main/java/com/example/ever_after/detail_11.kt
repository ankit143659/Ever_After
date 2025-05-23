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

class detail_11 : Fragment() {

    private lateinit var recyclerView : RecyclerView

    private lateinit var viewModel: dataViewModel

    private val interest = listOf(
        "Agnostic","Atheist","Buddhist","Catholic","Christian","Hindu","Jain","Jewish","Mormon","Muslim","Zoroastrian","Sikh","Spritual"
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_11, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)

        viewModel = ViewModelProvider(this)[dataViewModel::class.java]

        recyclerView.layoutManager = GridLayoutManager(context,2)


        var adapter = InterestsAdapter(interest,1,viewModel,requireActivity(),"Religion")
        recyclerView.adapter=adapter




        return view
    }

}