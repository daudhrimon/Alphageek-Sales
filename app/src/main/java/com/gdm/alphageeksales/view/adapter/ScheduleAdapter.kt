package com.gdm.alphageeksales.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Schedule
import com.gdm.alphageeksales.databinding.ScheduleItemsBinding
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.utils.showErrorToast
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ScheduleAdapter(
    private val dataList: List<Schedule>,
    private val context: Context
    ) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ScheduleItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ScheduleItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        fun bind(data: Schedule) {
            if (data.schedule_type == 1) {
                binding.visitScheduleLayout.isGONE()
                binding.detailsLayout.isGONE()
                binding.orderDelevaryScheduleLL.isVISIBLE()
                if (data.visit_status != 1){
                    binding.orderDelevaryScheduleLL.background.setColorFilter(Color.parseColor("#FF9800"), PorterDuff.Mode.SRC_ATOP);
                }
            } else {
                binding.orderDelevaryScheduleLL.isGONE()
                if (data.visit_status == 1) {
                    binding.visitScheduleLayout.isGONE()
                    binding.detailsLayout.isVISIBLE()
                } else {
                    binding.detailsLayout.isGONE()
                    binding.visitScheduleLayout.isVISIBLE()
                }
            }


            binding.orderDelevaryScheduleLL.setOnClickListener {
                val scheduledate = LocalDate.parse(data.schedule_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = Date()
                val currentDateee = LocalDate.parse(dateFormat.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val cmp = scheduledate.compareTo(currentDateee)
                if (cmp==0){
                if (data.visit_status == 1) {
                    val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEUTRAL -> {
                                dialog.dismiss()
                            }
                        }
                    }

                    val builder = AlertDialog.Builder(context,R.style.Calender_dialog_theme)
                    builder.setTitle(context.getString(R.string.order_oroduct_delevared))
                        .setNeutralButton("ok", dialogClickListener).show()
                } else {
                    Utils.currentSchedule = data
                    Navigation.createNavigateOnClickListener(R.id.orderProductDeliveryFragment).onClick(binding.orderDelevaryScheduleLL)
                } }
                else {
                    showErrorToast(context,"Future date schedule visit not allowed")
                }
            }


            binding.visitScheduleLayout.setOnClickListener {
                val scheduledate = LocalDate.parse(data.schedule_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = Date()
                val currentDateee = LocalDate.parse(dateFormat.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val cmp = scheduledate.compareTo(currentDateee)
                if (cmp==0){
                Utils.currentSchedule = data

                val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEUTRAL -> {
                                dialog.dismiss()
                            }
                            DialogInterface.BUTTON_POSITIVE -> {
                                Navigation.createNavigateOnClickListener(R.id.orderGenerationFragment)
                                    .onClick(binding.detailsLayout)
                                dialog.dismiss()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                                Navigation.createNavigateOnClickListener(R.id.storeVisitImageFragment)
                                    .onClick(binding.visitScheduleLayout)
                                dialog.dismiss()
                            }
                        }
                    }

                val builder = AlertDialog.Builder(context,R.style.Calender_dialog_theme)
                builder.setTitle(context.getString(R.string.are_you_ordering))
                    .setNeutralButton("Close",dialogClickListener)
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No",dialogClickListener)
                    .setCancelable(false)
                    .show()
                } else {
                    showErrorToast(context,"Future date schedule visit not allowed")
                }
            }
            // set up information
            binding.outletName.text = data.outlet_name
            binding.address.text = data.outlet_address
            binding.date.text = "Date : ${data.schedule_date}"
            binding.time.text = "Time : ${data.schedule_time}"
            binding.locationName.text = data.location_name

            binding.detailsLayout.setOnClickListener {
                val scheduledate = LocalDate.parse(data.schedule_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = Date()
                val currentDateee = LocalDate.parse(dateFormat.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val cmp = scheduledate.compareTo(currentDateee)
                if (cmp==0){
                val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEUTRAL -> {
                                dialog.dismiss()
                            }
                        }
                    }

                val builder = AlertDialog.Builder(context,R.style.Calender_dialog_theme)
                builder.setTitle(context.getString(R.string.already_visited))
                    .setNeutralButton("Ok", dialogClickListener).show()
                } else {
                    showErrorToast(context,"Future date schedule visit not allowed")
                }
            }
        }
    }

    inline fun <reified T : Any> Any.getPropertyValue(propertyName: String): T? {
        val getterName = "get" + propertyName.capitalize()
        return try {
            javaClass.getMethod(getterName).invoke(this) as? T
        } catch (e: NoSuchMethodException) {
            null
        }
    }
}