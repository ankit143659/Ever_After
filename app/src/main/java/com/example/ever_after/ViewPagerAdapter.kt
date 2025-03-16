package com.example.ever_after

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5 // Total fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> detail_1()
            1 -> detail_2()
            2 -> detail_3()
            3-> detail_4()
            4-> detail_5()
        else -> detail_1()
        }
    }
}
