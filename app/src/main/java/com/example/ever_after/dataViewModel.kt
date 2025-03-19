package com.example.ever_after

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class dataViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    private val _gender = MutableLiveData<String>()
    val name: LiveData<String> get() = _name
    val gender: LiveData<String> get() = _gender

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    fun updateName(newName: String) {
        if (newName.isNotBlank()) {  // Empty name store na ho
            _name.value = newName
        }
    }

    fun updateDate(dd: String, mm: String, yy: String) {
        if (dd.length == 2 && mm.length == 2 && yy.length == 4) {
            _date.value = "$dd/$mm/$yy"
        }
    }

    fun isDataValid(): Boolean {
        return !(_name.value.isNullOrEmpty() || _date.value.isNullOrEmpty())
    }

    fun updateGender(gender:String){
        if (gender.isNotBlank()){
            _gender.value = gender
        }
    }

    fun gendervalueSelected():Boolean{
        return !(_gender.value.isNullOrEmpty())
    }
}
