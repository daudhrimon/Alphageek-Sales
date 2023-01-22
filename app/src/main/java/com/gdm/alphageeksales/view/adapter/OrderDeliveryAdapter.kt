package com.gdm.alphageeksales.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.databinding.OrderDelivaryItemsBinding
import com.squareup.picasso.Picasso

class OrderDeliveryAdapter(private val dataList: ArrayList<SalesOrderData>) : RecyclerView.Adapter<OrderDeliveryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OrderDelivaryItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: OrderDelivaryItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SalesOrderData) {
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_: Exception){}
            data.product_name?.let { binding.productName.text = it }
            data.order_case_qty?.let { binding.caseCount.text = it.toString() }
            data.order_unit_qty?.let { binding.unitCount.text = it.toString() }
            data.unit_price?.let { binding.unitPrice.text = it.toString() }
            data.cost_price?.let { binding.costPrice.text = it.toString() }
            data.case_qty?.let {
                binding.stockCase.apply {
                    setText(" $it Case in Stock")
                    when { it < 10-> setTextColor(Color.RED)
                        it > 10-> setTextColor(Color.BLUE) }
                }
            }
            data.unit_qty?.let {
                binding.stockUnit.apply {
                    setText(" $it Unit in Stock")
                    when { it < 10-> setTextColor(Color.RED)
                        it > 10-> setTextColor(Color.BLUE) }
                }
            }


            when { (data.order_case_qty?:0) > (data.case_qty?:0)-> {
                binding.stockCase.apply {
                    error = ""
                    setTextColor(Color.RED)
                }
                binding.caseCount.setTextColor(Color.RED)
            }}
            when { (data.order_unit_qty?:0) > (data.unit_qty?:0)-> {
                binding.stockUnit.apply {
                    error = ""
                    setTextColor(Color.RED)
                }
                binding.unitCount.setTextColor(Color.RED)
            }}
        }
    }
    
    fun getList() = dataList
}