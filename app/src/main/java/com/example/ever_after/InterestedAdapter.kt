package com.example.ever_after

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class InterestsAdapter(private val interests: List<String>,private val maxSelection : Int) :
    RecyclerView.Adapter<InterestsAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<String>()

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
        updateChipStyle(holder.chip, item)

        // Click Listener
        holder.chip.setOnClickListener {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                if (selectedItems.size < maxSelection) {
                    selectedItems.add(item)
                }
            }
            updateChipStyle(holder.chip, item)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = interests.size

    private fun updateChipStyle(chip: Chip, item: String) {
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
