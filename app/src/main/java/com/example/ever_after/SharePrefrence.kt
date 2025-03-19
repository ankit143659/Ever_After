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

    fun saveData(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getData(key: String): String? {
        return prefs.getString(key, null)
    }

    fun clearData() {
        prefs.edit().clear().apply()
    }
}