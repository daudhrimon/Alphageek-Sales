package com.gdm.alphageeksales.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.databinding.ProductOrderItemsBinding
import com.gdm.alphageeksales.utils.ItemClickListener
import com.squareup.picasso.Picasso

class SaleOrderAdapter(private val dataList: ArrayList<SalesOrderData>,
                       private var listener: ItemClickListener,
                       private val saleType: Int?,
                       private var share: Boolean = false
) : RecyclerView.Adapter<SaleOrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductOrderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (share) {
            holder.binding.caseCount.isEnabled = false
            holder.binding.unitCount.isEnabled = false
            holder.binding.caseCount.setText(dataList[position].order_case_qty.toString())
            holder.binding.unitCount.setText(dataList[position].order_unit_qty.toString())
            holder.binding.unitPrice.text = dataList[position].unit_price.toString()
            val totalUnit = ((dataList[position].order_case_qty ?:1) * (dataList[position].unit_per_case ?: 1)) + (dataList[position].order_unit_qty?:1)
            holder.binding.costPrice.text = (totalUnit*(dataList[position].unit_price?:0.0)).toString()
        } else {
            holder.bind(dataList[position])
        }

    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(val binding: ProductOrderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SalesOrderData) {
            try { Picasso.get().load(data.product_image).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_: Exception){}
            data.product_name?.let { binding.productName.text = it }
            data.case_qty?.let {
                binding.stockCase.apply {
                    hint = "$it Case in Stock"
                    when { it < 10 -> setHintTextColor(Color.RED)
                        it > 10 -> setHintTextColor(Color.BLUE) }
                }
            }
            data.unit_qty?.let {
                binding.stockUnit.apply {
                    hint = "$it Unit in Stock"
                    when { it < 10 -> setHintTextColor(Color.RED)
                        it > 10 -> setHintTextColor(Color.BLUE) }
                }
            }
            data.unit_price?.let { binding.unitPrice.text = it.toString() }

            binding.caseCount.doAfterTextChanged {
                val caseCount = when { it != null && it.isNotEmpty()-> it.toString().toInt() else-> 0 }
                val unitCount = when { binding.unitCount.text.toString().isNotEmpty()-> binding.unitCount.text.toString().toInt() else-> 0 }
                if (saleType != null && saleType == 2) { // ready stock sale
                    if (caseCount <= (data.case_qty?:0) || caseCount == 0) {
                        binding.stockCase.apply {
                            setText("")
                            error = null
                        }
                    } else {
                        binding.stockCase.apply {
                            setText(" Out Of Stock Case")
                            error = ""
                        }
                    }
                    doCalculation(caseCount,unitCount,data,binding.costPrice)
                } else { // order generation
                    doCalculation(caseCount,unitCount,data,binding.costPrice)
                }
            }

            binding.unitCount.doAfterTextChanged {
                val caseCount = when { binding.caseCount.text.toString().isNotEmpty()-> binding.caseCount.text.toString().toInt() else-> 0 }
                val unitCount = when { it != null && it.isNotEmpty()-> it.toString().toInt() else-> 0 }
                if (saleType != null && saleType == 2) { // ready stock sale
                    if (unitCount <= (data.unit_qty?:0)  || unitCount == 0) {
                        binding.stockUnit.apply {
                            setText("")
                            error = null
                        }
                    } else {
                        binding.stockUnit.apply {
                            setText(" Out Of Stock Unit")
                            error = ""
                        }
                    }
                    doCalculation(caseCount, unitCount, data, binding.costPrice)
                } else { // order generation
                    doCalculation(caseCount, unitCount, data, binding.costPrice)
                }
            }
        }
    }

    private fun doCalculation(caseCount: Int, unitCount: Int, data: SalesOrderData, costPriceTv: TextView) {
        val totalUnit = when { caseCount != 0-> (caseCount * (data.unit_per_case?:1))+unitCount else -> unitCount }
        val costPrice = totalUnit * (data.unit_price?:0).toString().toDouble()
        costPriceTv.text = costPrice.toString()
        data.cost_price = costPrice
        data.order_case_qty = caseCount
        data.order_unit_qty = unitCount
        listener.onItemClick(0)
    }

    fun getList() = dataList
}