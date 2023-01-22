package com.gdm.alphageeksales.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.data.local.down_sync.Order
import com.gdm.alphageeksales.databinding.InvoiceListAdapterBinding
import com.gdm.alphageeksales.utils.InvoiceListItemClickListener

class InvoiceListAdapter(
    private var Order: ArrayList<Order>,
    private var listener: InvoiceListItemClickListener
) : RecyclerView.Adapter<InvoiceListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
        val binding = InvoiceListAdapterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") pos: Int) {
        val status = when(Order[pos].order_type.toString()){ "1"->{"Order Generation"} "2"->{"Ready Stock Sale"} "3"->{"Order Product Delivery"} else->""}
        holder.binding.tvorderId.text = Order[pos].order_id.toString()
        holder.binding.orderType.text = status
        holder.binding.tvOutletName.text = Order[pos].outlet_name.toString()
        holder.binding.tvorderDate.text = Order[pos].order_date.toString()
        holder.binding.tvGrandTotal.text = Order[pos].grand_total.toString()
        holder.binding.tvOutletPhone.text = Order[pos].outlet_phone.toString()
        holder.binding.tvGrandTotal.text = Order[pos].grand_total.toString()

        holder.itemView.setOnClickListener {
            listener.onInvoiceListItemClick(Order[pos])
        }
    }
    override fun getItemCount(): Int {
        return Order.size
    }
    inner class ViewHolder(val binding: InvoiceListAdapterBinding) : RecyclerView.ViewHolder(binding.root)
}