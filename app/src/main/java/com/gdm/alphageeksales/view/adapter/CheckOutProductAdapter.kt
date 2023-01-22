package com.gdm.alphageeksales.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.CheckoutProducts
import com.gdm.alphageeksales.databinding.VhCheckoutProductBinding
import com.squareup.picasso.Picasso

class CheckOutProductAdapter(private val products: List<CheckoutProducts?>?) : RecyclerView.Adapter<CheckOutProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = VhCheckoutProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products!![position])
    }
    override fun getItemCount(): Int {
        return products!!.size
    }

    inner class ViewHolder(val binding: VhCheckoutProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(checkOutProduct: CheckoutProducts?) {
            try { Picasso.get().load(checkOutProduct?.product_image.toString()).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.productImage) } catch (_: Exception) {}
            checkOutProduct?.product_name?.let { binding.productName.text = it }
            checkOutProduct?.case_qty?.let { binding.productCase.text = "Case:  $it" }
            checkOutProduct?.unit_qty?.let { binding.productUnit.text = "Unit:    $it" }
        }
    }
}