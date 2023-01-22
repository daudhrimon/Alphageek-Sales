package com.gdm.alphageeksales.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Inventory
import com.gdm.alphageeksales.databinding.InventoryAdapterItemsBinding
import com.squareup.picasso.Picasso
import java.lang.Exception

class InventoryAdapter (private val dataList: ArrayList<Inventory>) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding  = InventoryAdapterItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: InventoryAdapterItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Inventory) {
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_: Exception){}
            data.product_name?.let { binding.productName.text = it }
            data.client_name?.let { binding.clientName.text = it }
            data.case_qty?.let {
                binding.caseCount.apply {
                    text = "$it Case in Stock"
                    when { it < 10-> setTextColor(Color.RED)
                        it > 10-> setTextColor(Color.BLUE) }
                }
            }
            data.unit_qty?.let {
                binding.unitCount.apply {
                    text = "$it Unit in Stock"
                    when { it < 10-> setTextColor(Color.RED)
                        it > 10-> setTextColor(Color.BLUE) }
                }
            }
        }
    }
}