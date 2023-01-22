package com.gdm.alphageeksales.view.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.databinding.OutletItemsBinding
import com.google.gson.Gson

class OutletListAdapter(
    private val dataList: List<Outlet>,
    private val context: Context
) : RecyclerView.Adapter<OutletListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OutletItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: OutletItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Outlet) {
            binding.outletName.text = data.outlet_name
            binding.address.text    = data.outlet_address?:""
            binding.phone.text      = "phone : ${data.outlet_phone}"

            binding.editBtn.setOnClickListener {
                AlertDialog.Builder(context,R.style.Calender_dialog_theme)
                    .setTitle("Update Outlet")
                    .setMessage("Do you Want to update this outlet ?")
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                        val bundle = Bundle()
                        bundle.putString("OUTLET", Gson().toJson(data))
                        Navigation.findNavController(context as Activity, R.id.navHostFragment).navigate(R.id.createOutletFragment,bundle)
                    })
                    .setCancelable(false)
                    .show()
            }
        }
    }
}