package com.gdm.alphageeksales.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageek.data.local.down_sync.RoutePlanDetails
import com.gdm.alphageeksales.databinding.RouteItemsBinding

class LocationAdapter(private val dataList: List<RoutePlanDetails>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RouteItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: RouteItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RoutePlanDetails) {
            binding.locationName.text = data.location_name
        }
    }
}