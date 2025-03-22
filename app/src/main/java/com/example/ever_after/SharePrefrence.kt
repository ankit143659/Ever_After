package com.example.ever_after

import android.content.Context
import android.content.SharedPreferences

class SharePrefrence(context: Context){
    private val PREF_NAME = "EverAfterPrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


    fun loginState(State : Boolean){
        val editor = prefs.edit()
        editor.putBoolean("loginState",State)
        editor.apply()
    }

    fun checkLoginState():Boolean{
        return prefs.getBoolean("loginState",false)
    }

    fun name(name : String){
        val editor = prefs.edit()
        editor.putString("name",name)
        editor.apply()
    }

    fun getName() : String? {
        return prefs.getString("name","")
    }

    fun DetailState(State : Boolean){
        val editor = prefs.edit()
        editor.putBoolean("detailState",State)
        editor.apply()
    }

    fun checkDetailState():Boolean{
        return prefs.getBoolean("detailState",false)
    }

}