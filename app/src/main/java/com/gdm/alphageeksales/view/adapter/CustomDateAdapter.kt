package com.gdm.alphageeksales.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.dates.DatesModel
import com.gdm.alphageeksales.databinding.DateRecyclerItemBinding
import com.gdm.alphageeksales.viewmodels.DashboardViewModel

class CustomDateAdapter(
    private val dataList: List<DatesModel>,
    private val context: Context,
    private val dashboardViewModel: Any
    ) : RecyclerView.Adapter<CustomDateAdapter.ViewHolder>() {

    private var rowIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DateRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: DateRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DatesModel) {
            binding.dayName.text = data.dates
            binding.dayDate.text = data.dayName

            binding.dateCard.setOnClickListener {
                if (dashboardViewModel is DashboardViewModel){
                    dashboardViewModel.getScheduleList(data.fullDates)
                }
                rowIndex = adapterPosition
                notifyDataSetChanged()
            }

            if (rowIndex == adapterPosition ) {
                binding.dateCard.backgroundTintList = ContextCompat.getColorStateList(context,R.color.colorPrimaryDark)
                binding.dayDate.setTextColor(ContextCompat.getColor(context,R.color.white))
                binding.dayName.setTextColor(ContextCompat.getColor(context,R.color.white))

            } else {
                binding.dateCard.backgroundTintList = ContextCompat.getColorStateList(context,R.color.white)
                binding.dayDate.setTextColor(ContextCompat.getColor(context,R.color.black))
                binding.dayName.setTextColor(ContextCompat.getColor(context,R.color.black))
            }
        }
    }

    fun getRowIndex() = rowIndex
}