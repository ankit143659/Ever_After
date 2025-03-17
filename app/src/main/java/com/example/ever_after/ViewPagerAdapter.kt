package com.example.ever_after

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 12// Total fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> detail_1()
            1 -> detail_2()
            2 -> detail_3()
            3-> detail_4()
            4-> detail_5()
            5-> detail_6()
            6-> detail_7()
            7-> detail_8()
            8-> detail_9()
            9-> detail_10()
            10-> detail_11()
            11-> detail_12()
        else -> detail_1()
        }
    }
}
