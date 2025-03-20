package com.example.ever_after

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class detail_1 : Fragment() {

    private lateinit var name: EditText
    private lateinit var dd: EditText
    private lateinit var mm: EditText
    private lateinit var yy: EditText

    private lateinit var nameError: TextView
    private lateinit var dateError: TextView

    private lateinit var sharePrefrence: SharePrefrence

    private val viewModel: dataViewModel by activityViewModels()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_1, container, false)

        sharePrefrence = SharePrefrence(requireContext())

        name = view.findViewById(R.id.etFirstName)
        dd = view.findViewById(R.id.dd)
        mm = view.findViewById(R.id.mm)
        yy = view.findViewById(R.id.yy)
        database = FirebaseDatabase.getInstance().getReference("Users")

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Current User ka UID
            database = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Details")
        } else {
            database = FirebaseDatabase.getInstance().getReference("Users").child("Unknown")
        }

        nameError = view.findViewById(R.id.nameError)
        dateError = view.findViewById(R.id.dateError)

        // Name Real-time Update
        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nameValue = s.toString().trim()
                if (nameValue.isNotEmpty()) {
                    viewModel.updateName(nameValue)
                    nameError.visibility = View.GONE
                    sharePrefrence.name(nameValue)
                    database.child("name").setValue(nameValue)  // Firebase me real-time store
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Date Auto Format and Cursor Shift
        dd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2) mm.requestFocus()  // Cursor Shift to MM
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        mm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2) yy.requestFocus()  // Cursor Shift to YY
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        yy.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (dd.text.length == 2 && mm.text.length == 2 && yy.text.length == 4) {
                    val dateValue = "${dd.text}/${mm.text}/${yy.text}"
                    viewModel.updateDate(dd.text.toString(), mm.text.toString(), yy.text.toString())
                    dateError.visibility = View.GONE
                    database.child("DOB").setValue(dateValue)  // Firebase me real-time update
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }
}
