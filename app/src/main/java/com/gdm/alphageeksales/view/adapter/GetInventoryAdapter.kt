package com.gdm.alphageeksales.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Product
import com.gdm.alphageeksales.data.remote.InventoryRequest
import com.gdm.alphageeksales.databinding.GetInventoryItemsBinding
import com.squareup.picasso.Picasso
import java.lang.Exception

class GetInventoryAdapter(private val dataList: ArrayList<Product>) : RecyclerView.Adapter<GetInventoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GetInventoryItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: GetInventoryItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Product) {
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (e: Exception) {/**/}
            val caseQty = data.case_qty
            val unitQty = data.unit_qty
            binding.productName.text = data.product_name
            binding.caseCount.setText(when{caseQty != null && caseQty > 0-> caseQty.toString() else->""})
            binding.unitCount.setText(when{unitQty != null && unitQty > 0-> unitQty.toString() else->""})

            binding.caseCount.doAfterTextChanged {
                val caseCount = when { it != null && it.isNotEmpty()-> it.toString().toInt() else-> 0 }
                val unitCount = when { binding.unitCount.text.toString().isNotEmpty()-> binding.unitCount.text.toString().toInt() else-> 0 }
                data.case_qty = caseCount
                data.unit_qty = unitCount
            }

            binding.unitCount.doAfterTextChanged {
                val caseCount = when { binding.caseCount.text.toString().isNotEmpty()-> binding.caseCount.text.toString().toInt() else-> 0 }
                val unitCount = when { it != null && it.isNotEmpty()-> it.toString().toInt() else-> 0 }
                data.case_qty = caseCount
                data.unit_qty = unitCount
            }
        }
    }

    fun getList() = dataList
}