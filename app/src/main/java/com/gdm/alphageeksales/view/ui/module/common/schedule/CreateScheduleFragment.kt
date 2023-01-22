package com.gdm.alphageeksales.view.ui.module.common.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.data.local.down_sync.Schedule
import com.gdm.alphageeksales.databinding.FragmentCreateScheduleBinding
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.utils.showSuccessToast
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import com.gdm.alphageeksales.viewmodels.OutletViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateScheduleFragment : Fragment() {
    private lateinit var binding: FragmentCreateScheduleBinding
    private val outletViewModel: OutletViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var outletList: ArrayList<Outlet> = ArrayList()
    private var outlet: Outlet? = null
    private var date: String = "0000-00-00"
    private var time: String = "00:00:00"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateScheduleBinding.inflate(layoutInflater)

        // get all outlet
        outletViewModel.getAllOutlet()
        outletViewModel.outletList.observe(viewLifecycleOwner) {
            if (it != null) {
                outletList.clear()
                outletList.add(
                    Outlet(
                        -1,
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "Select Outlet",
                        "",
                        -1,
                        -1,
                        -1,
                        -1,
                        "",
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "",
                        0,
                        1,
                        0,
                        "",
                        null
                    )
                )
                outletList.addAll(it)
                binding.outletSpinner.adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    outletList
                )
            }
        }

        // get selected outlet
        binding.outletSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem != "Select Outlet" && selectedItem != null) {
                    outlet = outletList[binding.outletSpinner.selectedItemPosition]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        //setup calender
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { date_picker, year, monthOfYear, dayOfMonth ->
            date_picker.minDate = System.currentTimeMillis() - 1000
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            date = sdf.format(calendar.time)
            binding.dateTv.text = date
        }

        binding.scheduleDateLayout.setOnClickListener {
            DatePickerDialog(
                requireActivity(),
                R.style.Calender_dialog_theme,
                dateSetListener,
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.scheduleTimeLayout.setOnClickListener {
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val mTimePicker = TimePickerDialog(
                requireActivity(), R.style.Calender_dialog_theme,
                { _, selectedHour, selectedMinute ->
                    val finalHour = when(selectedHour.toString()){"0","00"->"24" else-> selectedHour}
                    val finalMinute = when(selectedMinute.toString().length) {1-> "0${selectedMinute}" else-> selectedMinute.toString()}
                    time = "$finalHour:$finalMinute:00"
                    binding.timeTv.text = time
                }, hour, minute, true
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }

        binding.saveBtn.setOnClickListener {
            when {
                outlet == null -> {
                    showErrorToast(requireContext(),"Please select Outlet")
                }
                date == "0000-00-00" -> {
                    showErrorToast(requireContext(),"Please select date")
                }
                time == "00:00:00" -> {
                    showErrorToast(requireContext(),"Please select time")
                }
                else -> {
                    dashboardViewModel.insertNewSchedule(
                        Schedule(
                            System.currentTimeMillis(),
                            outlet!!.outlet_id,
                            "9.072264",
                            "7.491302",
                            outlet?.location_name,
                            outlet?.street_no + ", " + outlet?.street_name,
                            outlet?.outlet_name,
                            date,
                            time,
                            outlet?.country_id,
                            0,
                            outlet?.state_id,
                            outlet?.region_id,
                            outlet?.location_id,
                            is_local = 1,
                            pre_schedule_id = 0,
                            pre_order_id = 0,
                            schedule_type = 0
                        )
                    )
                }
            }

        }


        // create schedule response
        dashboardViewModel.insertSchedule.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1) {
                    dashboardViewModel.getDashboardData()
                } else {
                    showErrorToast(requireContext(),"Failed to create Schedule")
                }
            }
        }

        dashboardViewModel.dashboardData.observe(viewLifecycleOwner) {
            if (it != null) {
                val dashboard = it
                try {
                    if (isToday(date)) {
                        dashboard.sales_visit = (dashboard.sales_visit?:0)+1
                        dashboardViewModel.updateDashboardData(dashboard)
                    }
                } catch (_: Exception) {/**/}
                showSuccessToast(requireContext(),"Schedule created successfully")
                findNavController().navigate(R.id.dashboardFragment)
            }
        }


        // handle error
        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) {
            showErrorToast(requireContext(),it.toString())
        }


        return binding.root
    }

    private fun isToday(visitDate: String?): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val calendar = Calendar.getInstance()
            val currentDate = sdf.format(calendar.time)
            visitDate.equals(currentDate)
        } catch (e: Exception){
            false
        }
    }
}