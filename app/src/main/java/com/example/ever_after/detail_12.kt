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


class detail_12 : Fragment() {
  private lateinit var recyclerView: RecyclerView
  private val interest = listOf(
      "Black lives matter","Feminism","Enviromentalism","Trans rights","Disability rights","Reproductive rights"
  )
    private lateinit var viewModel: dataViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_detail_12, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        viewModel = ViewModelProvider(this).get(dataViewModel::class.java)

        recyclerView.layoutManager = GridLayoutManager(context,1)


        val adapter = InterestsAdapter(interest,3,viewModel,requireActivity(),"Communities")
        recyclerView.adapter=adapter
        return view
    }

}