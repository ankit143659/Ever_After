package com.example.ever_after

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class interesetAdapter2(private val interests: List<String>,private val maxSelection : Int,private val viewModel: dataModel2? = null,private val lifecycleOwner: LifecycleOwner? = null,private val name : String?=null) :
    RecyclerView.Adapter<interesetAdapter2.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chip: Chip = view.findViewById(R.id.chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chip_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = interests[position]
        holder.chip.text = item

        // Set initial state
        if (lifecycleOwner != null) {
            viewModel?.selectedInterests?.observe(lifecycleOwner) { selectedItems ->
                updateChipStyle(holder.chip, item, selectedItems)
            }
        }

        holder.chip.setOnClickListener {
            if (name != null) {
                viewModel?.toggleInterest(item, maxSelection,name)
            }  // ViewModel ko update bhejo
        }
    }

    override fun getItemCount(): Int = interests.size

    private fun updateChipStyle(chip: Chip, item: String, selectedItems: Set<String>) {
        val context = chip.context

        if (selectedItems.contains(item)) {
            chip.setChipBackgroundColorResource(R.color.chip_selected)
            chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.baseline_close_24)
        } else {
            chip.setChipBackgroundColorResource(R.color.chip_default)
            chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.baseline_add_24)
        }
    }
}
