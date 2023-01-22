package com.gdm.alphageeksales.view.ui.module.sales_and_order

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.data.local.image_model.ImageModel
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit
import com.gdm.alphageeksales.databinding.FragmentOrderGenerationImageBinding
import com.gdm.alphageeksales.utils.ProgressLoader
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.utils.showSuccessToast
import com.gdm.alphageeksales.viewmodels.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList

@AndroidEntryPoint
class OrderGenerationImageFragment : Fragment() {
    private lateinit var binding: FragmentOrderGenerationImageBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val outletViewModel: ImageNoteViewModel by viewModels()
    private val visitViewModel: VisitViewModel by viewModels()
    private var outletData: Outlet? = null
    private var imageUri: Uri? = null
    private var saleType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderGenerationImageBinding.inflate(layoutInflater)
        saleType = arguments?.getString("sale_type").toString()
        ProgressLoader.init(requireContext())

        Log.wtf("order_list",arguments?.getString("order_gen_list").toString())

        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule?.outlet_id)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
            }
        }

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            throwable.localizedMessage?.let {
                ProgressLoader.dismiss()
                showErrorToast(requireContext(),"Something went wrong !")
            }
        }

        binding.addImage.setOnClickListener {
            ImagePicker.with(this)
                .crop() //Crop image(Optional), Check Customization for more option
                .compress(1024) //Final image size will be less than 1 MB(Optional)
                .cameraOnly()
                .maxResultSize(1080,1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.shareBtn.setOnClickListener {
            val status = when(saleType) { "1"-> {"Order Generation"} "2"-> {"Ready Stock Sale"} "3"-> {"Order Product Delivery"} else-> "" }
            when {
                imageUri == null -> {
                    showErrorToast(requireContext(),"Upload image first")
                }
                binding.notes.text.isEmpty() -> {
                    showErrorToast(requireContext(),"Write something.....")
                }
                else -> {
                    OrderGenerationShareDialog(
                        requireContext(),
                        "Sales & Order Generator\n($status)",
                        "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                        binding.notes.text.toString(),
                        arguments?.getString("order_gen_list").toString(),
                        imageUri,
                        arguments?.getString("total_sale"),
                        arguments?.getString("paid_amount"),
                        arguments?.getString("due_amount"),
                        arguments?.getString("change_amount"),
                        arguments?.getString("payment_type_text")
                    ).show()
                }
            }
        }


        binding.completeBtn.setOnClickListener {
            when {
                imageUri == null -> {
                    showErrorToast(requireContext(),"Upload image first")
                }
                binding.notes.text.isEmpty() -> {
                    showErrorToast(requireContext(),"Write something.....")
                }
                saleType.isNullOrEmpty() -> {
                    showErrorToast(requireContext(),"Something went wrong !")
                }
                else -> {
                    val visitDistance = Utils.getDistance(
                        (outletData?.gio_lat ?: "0.0").toDouble(),
                        (outletData?.gio_long ?: "0.0").toDouble(),
                        (Utils.gio_lat ?: "0.0").toDouble(),
                        (Utils.gio_long ?: "0.0").toDouble()
                    )
                    val isException = when { visitDistance <= 100.00-> "0" else-> "1" }

                    val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
                    val calendar = Calendar.getInstance()
                    Utils.endTime = currentTime.format(calendar.time)

                    ProgressLoader.show()
                    val images = ArrayList<ImageModel>()
                    images.add(ImageModel(Utils.convertImageToBase64(requireActivity().contentResolver,imageUri!!).replace("\n", "").replace(" ", "")))

                    try {
                        // ready Stock Sale or Order Generation
                        visitViewModel.insertVisitData(
                            ScheduleVisit(
                                System.currentTimeMillis(),
                                Utils.currentSchedule?.schedule_id,
                                Utils.currentSchedule?.outlet_id,
                                outlet_type_id = outletData?.type_id,
                                outlet_channel_id = outletData?.channel_id,
                                visit_date = Utils.currentSchedule?.schedule_date!!,
                                visit_time = Utils.currentSchedule?.schedule_time!!,
                                country_id = Utils.currentSchedule?.country_id!!,
                                state_id = Utils.currentSchedule?.state_id,
                                region_id = Utils.currentSchedule?.region_id,
                                location_id = Utils.currentSchedule?.location_id,
                                when(saleType){"2"->2/*Ready Stock Sales*/ "1"->3/*Order Generation*/ "3"->4/*Order Product Delivery*/ else->0},
                                image_list = Gson().toJson(images),
                                notes = binding.notes.text?.toString(),
                                order_id = when(saleType){"3"->Utils.orderIdDelivery.toString()/*Order Product Delivery*/ else->Utils.orderIdGlobal.toString()},
                                order_type = saleType,
                                payment_type = arguments?.getString("payment_type").toString(),
                                totalsale = arguments?.getString("total_sale")?.toString(),
                                paid_amount = arguments?.getString("paid_amount")?.toString(),
                                due_amount = arguments?.getString("due_amount")?.toString(),
                                change_amount = arguments?.getString("change_amount")?.toString(),
                                sales_order_list = arguments?.getString("order_list").toString(),
                                order_date = Utils.currentSchedule?.schedule_date,
                                order_time = Utils.currentSchedule?.schedule_time,
                                pre_schedule_id = Utils.currentSchedule?.pre_schedule_id,
                                pre_order_id = Utils.currentSchedule?.pre_order_id,
                                schedule_type = Utils.currentSchedule?.schedule_type,
                                stat_time = Utils.startTime,
                                end_time = Utils.endTime,
                                gio_lat = Utils.gio_lat ?: "0.0",
                                gio_long = Utils.gio_long ?: "0.0",
                                is_exception = isException,
                                visit_distance = visitDistance.toString(),
                                isInternetAvailable = Utils.checkForInternet(requireActivity())
                            )
                        )
                    } catch (e: Exception) {
                        ProgressLoader.dismiss()
                        showErrorToast(requireContext(),"Something went wrong !")
                    }
                }
            }
        }


        visitViewModel.insertVisitResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1) {
                    lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
                        val salesOrderList = Gson().fromJson<ArrayList<SalesOrderData>>(
                            arguments?.getString("order_list").toString(),
                            object : TypeToken<ArrayList<SalesOrderData>>(){}.type
                        )
                        val orderItem = Order(
                            Utils.orderIdGlobal,
                            Utils.orderIdGlobal,
                            Utils.currentSchedule?.schedule_id,
                            Utils.currentSchedule?.outlet_id,
                            Utils.currentSchedule?.country_id,
                            Utils.currentSchedule?.region_id,
                            Utils.currentSchedule?.state_id,
                            Utils.currentSchedule?.location_id,
                            grand_total = (arguments?.getString("total_sale")?:"0.0").toDouble(),
                            paid_amount = (arguments?.getString("paid_amount")?:"0.0").toDouble(),
                            due_amount = (arguments?.getString("due_amount")?:"0.0").toDouble(),
                            change_amount = (arguments?.getString("change_amount")?:"0.0").toDouble(),
                            payment_type = arguments?.getString("payment_type").toString(),
                            order_type = arguments?.getString("sale_type")?.toDouble()!!.toLong(),
                            order_date = Utils.currentSchedule?.schedule_date,
                            "",
                            outlet_name = outletData?.outlet_name,
                            Utils.username,
                            outletData?.outlet_phone!!
                        )
                        val orderDetails = ArrayList<OrderDetails>()
                        for (item in salesOrderList) {
                            val orderDetailsLocal = OrderDetails(
                                id = 0,
                                product_id = item.product_id,
                                order_id = Utils.orderIdGlobal,
                                unit_per_case = item.unit_per_case,
                                price = item.cost_price,
                                product_name = item.product_name,
                                category_id = item.product_category_id,
                                brand_id = item.brand_id,
                                sales_price = item.cost_price,
                                product_image = item.product_image,
                                client_id = item.client_id,
                                order_case_qty = item.order_case_qty,
                                order_unit_qty = item.order_unit_qty
                            )
                            orderDetails.add(orderDetailsLocal)
                        }
                        withContext(Dispatchers.Main) {
                            val scheduleItem = Utils.currentSchedule
                            scheduleItem?.outlet_visit = 1
                            scheduleItem?.visit_status = 1
                            scheduleItem?.merchandising_visit = 1
                            dataSyncViewModel.saveOrdersLocalDb(orderItem,orderDetails,scheduleItem)
                        }
                    }
                } else {
                    ProgressLoader.dismiss()
                    showErrorToast(requireContext(),"Failed to visit")
                }
            }
        }

        dataSyncViewModel.orderGenResponse.observe(viewLifecycleOwner) {
            ProgressLoader.dismiss()
            if (it != null && it.isNotEmpty() && it == "success") {
                when (saleType) {
                    "1" -> { // for order generation create pre schedule
                        val date = arguments?.getString("date")
                        val time = arguments?.getString("time")
                        if (!date.isNullOrEmpty() && !time.isNullOrEmpty()) {
                            dashboardViewModel.insertNewSchedule(
                                Schedule(
                                    System.currentTimeMillis(),
                                    outletData!!.outlet_id,
                                    "9.072264",
                                    "7.491302",
                                    outletData?.location_name,
                                    outletData?.street_no + ", " + outletData?.street_name,
                                    outletData?.outlet_name,
                                    arguments?.getString("date"),
                                    arguments?.getString("time"),
                                    outletData?.country_id,
                                    0,
                                    outletData?.state_id,
                                    outletData?.region_id,
                                    outletData?.location_id,
                                    is_local = 1,
                                    pre_schedule_id = Utils.currentSchedule?.schedule_id,
                                    pre_order_id = Utils.orderIdGlobal,
                                    schedule_type = 1
                                )
                            )
                        } else {
                            ProgressLoader.dismiss()
                            showErrorToast(requireContext(),"Failed to create Pre Schedule")
                        }
                    }
                    else -> {
                        // for ready Stock and order product delivery
                        visitViewModel.updateInventory(Utils.inventoryChangeList)
                        dashboardViewModel.getDashboardData()
                    }
                }
            } else {
                ProgressLoader.dismiss()
                showSuccessToast(requireContext(),"Something went wrong !")
            }
        }

        // create schedule response
        dashboardViewModel.insertSchedule.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1) { // for order generation create pre schedule
                    dashboardViewModel.getDashboardData()
                } else {
                    ProgressLoader.dismiss()
                    showErrorToast(requireContext(),"Failed to create Pre Schedule")
                }
            } else {
                ProgressLoader.dismiss()
                showSuccessToast(requireContext(),"Something went wrong !")
            }
        }

        dashboardViewModel.dashboardData.observe(viewLifecycleOwner) {
            if (it != null) {
                val dashboard = it
                dashboard.visited_sales = (dashboard.visited_sales?:0)+1

                when (saleType) { // for order generation create pre schedule
                    "1" -> {
                        try {
                            if (isToday((arguments?.getString("date")?:"0000-00-00"))) {
                                dashboard.sales_visit = (dashboard.sales_visit?:0)+1
                                dashboard.daily_generat_amount = ((dashboard.daily_generat_amount?:0.0)+(arguments?.getString("total_sale")?:"0.0").toDouble())
                                dashboardViewModel.updateDashboardData(dashboard)
                            } else {
                                dashboard.daily_generat_amount = ((dashboard.daily_generat_amount?:0.0)+(arguments?.getString("total_sale")?:"0.0").toDouble())
                                dashboardViewModel.updateDashboardData(dashboard)
                            }
                        } catch (_: Exception){}
                        showSuccessToast(requireContext(),"Pre Schedule created successfully")
                        findNavController().navigate(R.id.dashboardFragment)
                    }
                    "2" -> { // for ready stock sales
                        dashboard.daily_sales_amount = ((dashboard.daily_sales_amount?:0.0)+(arguments?.getString("total_sale")?:"0.0").toDouble())
                        dashboardViewModel.updateDashboardData(dashboard)
                        showSuccessToast(requireContext(),"Successfully Visited")
                        findNavController().navigate(R.id.dashboardFragment)
                    }
                    "3" -> { // for order product delivery
                        dashboardViewModel.updateDashboardData(dashboard)
                        showSuccessToast(requireContext(),"Successfully Visited")
                        findNavController().navigate(R.id.dashboardFragment)
                    }
                }
            } else {
                ProgressLoader.dismiss()
                showSuccessToast(requireContext(),"Something went wrong")
            }
        }

        // handle error from Dashboard ViewModel
        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) {
            ProgressLoader.dismiss()
            showErrorToast(requireContext(),it.toString())
        }

        return binding.root
    }

    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    imageUri = fileUri
                    binding.addImage.setImageURI(fileUri)
                    Utils.convertImageToBase64(requireActivity().contentResolver, fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireActivity(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
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