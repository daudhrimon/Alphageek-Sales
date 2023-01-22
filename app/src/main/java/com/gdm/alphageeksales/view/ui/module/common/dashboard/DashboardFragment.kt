package com.gdm.alphageeksales.view.ui.module.common.dashboard

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.dates.DatesModel
import com.gdm.alphageeksales.databinding.FragmentDashboardBinding
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.view.adapter.CustomDateAdapter
import com.gdm.alphageeksales.view.adapter.ScheduleAdapter
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val dashboardViewModel:DashboardViewModel by viewModels()
    private var customDateAdapter: CustomDateAdapter? = null
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var planed = 0

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater)

        setupDates()

        // dashboard data
        dashboardViewModel.dashboardData.observe(viewLifecycleOwner) {
            if (it != null){
                binding.visitPlanned.text        = "Planned : $planed"
                binding.totalLogin.text          = "Total : ${it.login_count ?: "0"}"
                if ((customDateAdapter?.getRowIndex()?:0) == 0) {
                    binding.actualVisit.text     = "Actual    : ${it.visited_sales?:0}"
                    binding.pendingVisit.text    = "Pending : ${planed-(it.visited_sales?:0)}"
                    binding.totalSale.text       = "Amount : ${it.daily_sales_amount?:0.0}"
                    binding.orderGeneration.text = "Amount : ${it.daily_generat_amount?:0.0}"
                } else {
                    binding.actualVisit.text     = "Actual    : 0"
                    binding.pendingVisit.text    = "Pending : $planed"
                    binding.totalSale.text       = "Amount : 0.0"
                    binding.orderGeneration.text = "Amount : 0.0"
                }
            }
        }

        // get  current date schedule data
        val calendar = Calendar.getInstance()
        dashboardViewModel.getScheduleList(simpleDateFormat.format(calendar.time))
        dashboardViewModel.scheduleList.observe(viewLifecycleOwner){
            if (it != null){
                planed = it.size
                binding.totalVisit.text = "You Have $planed Store Visit Today"
                dashboardViewModel.getDashboardData()
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = ScheduleAdapter(it,requireContext())
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }


        //setup calender
        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)

                dashboardViewModel.getScheduleList(sdf.format(myCalendar.time))
            }

        binding.calenderBtn.setOnClickListener{
            DatePickerDialog(requireActivity(), R.style.Calender_dialog_theme,
                dateSetListener,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }


        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupDates() {
        val dateFormat: DateFormat = SimpleDateFormat("dd")
        val dayFormat: DateFormat = SimpleDateFormat("EEE")
        val calendar = Calendar.getInstance()
        val datesModelList: MutableList<DatesModel> = ArrayList()
        for (i in 0..14) {
            datesModelList.add(DatesModel(dayFormat.format(calendar.time),dateFormat.format(calendar.time),simpleDateFormat.format(calendar.time)))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        customDateAdapter = CustomDateAdapter(datesModelList, requireContext(),dashboardViewModel)
        binding.dateRecyclerview.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.HORIZONTAL,false)
        binding.dateRecyclerview.adapter = customDateAdapter
    }
}