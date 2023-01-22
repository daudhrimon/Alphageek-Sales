package com.gdm.alphageeksales.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Checkout
import com.gdm.alphageeksales.databinding.CheckoutAdapterBinding
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE

class CheckOutMainAdapter(
    private val dataList: ArrayList<Checkout>,
    private val context: Context
    ) : RecyclerView.Adapter<CheckOutMainAdapter.ViewHolder>() {

    private var itemList = ArrayList<String>()
    private var index = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CheckoutAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: CheckoutAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Checkout) {

            when { (data.checkout_type?:1) != 1-> binding.checkApprove.isVISIBLE() }

            when(index) {
                adapterPosition -> {
                    when { dataList[adapterPosition].checkout_product != null ->
                        when { dataList[adapterPosition].checkout_product!!.isNotEmpty() -> {
                            val adapter = CheckOutProductAdapter(dataList[adapterPosition].checkout_product)
                            binding.expandRecycler.isVISIBLE()
                            binding.expandBtn.icon = ContextCompat.getDrawable(context,R.drawable.ic_baseline_expand_less_24)
                            binding.expandRecycler.adapter = adapter
                        } }
                    }
                }

                else -> {
                    binding.expandRecycler.isGONE()
                    binding.expandBtn.icon = ContextCompat.getDrawable(context,R.drawable.ic_baseline_expand_more_24)
                }
            }

            binding.checkApprove.setOnCheckedChangeListener { checkBox, _ ->
                when{
                    checkBox.isChecked -> {
                        itemList.remove(data.checkout_id.toString())
                        itemList.add(data.checkout_id.toString())
                    }
                    else -> { itemList.remove(data.checkout_id.toString()) }
                }
            }


            binding.checkoutId.text = data.checkout_id.toString()
            binding.checkoutDate.text = data.checkout_date.toString()


            binding.expandBtn.setOnClickListener {
                index = when { binding.expandRecycler.isVisible -> -1 else -> adapterPosition }
                notifyDataSetChanged()
            }
        }
    }

    fun getList() = itemList
}