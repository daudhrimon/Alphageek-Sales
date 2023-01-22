package com.gdm.alphageeksales.view.ui.module.sales_and_order

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.data.local.image_model.ImageModel
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit
import com.gdm.alphageeksales.databinding.FragmentStoreVisitImageBinding
import com.gdm.alphageeksales.utils.ProgressLoader
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.utils.showSuccessToast
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import com.gdm.alphageeksales.viewmodels.OutletViewModel
import com.gdm.alphageeksales.viewmodels.VisitViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class StoreVisitImageFragment : Fragment() {
    private lateinit var binding: FragmentStoreVisitImageBinding
    private val visitViewModel: VisitViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val outletViewModel : OutletViewModel by viewModels()
    private var outletData : Outlet? = null
    private var imageUri: Uri? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreVisitImageBinding.inflate(layoutInflater)
        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        val calendar = Calendar.getInstance()
        Utils.startTime = currentTime.format(calendar.time)
        ProgressLoader.init(requireContext())

        // get current outlet info
        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id!!)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
            }
        }

        binding.image.setOnClickListener {
            ImagePicker.with(this)
                .crop()                                    //Crop image(Optional), Check Customization for more option
                .compress(1024)                    //Final image size will be less than 1 MB(Optional)
                .cameraOnly()
                .maxResultSize(1080,1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.shareBtn.setOnClickListener {
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
                        "Sales & Order Generator\n(Store Visit)",
                        "Outlet Name: ${outletData?.outlet_name ?: ""},\n Outlet Address: ${outletData?.outlet_address ?: ""},\nOutlet Phone: ${outletData?.outlet_phone ?: ""}",
                        binding.notes.text.toString(),
                        null,
                        imageUri,
                        null,
                        null,
                        null,
                        null,
                        null
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
                else -> {
                    val imageList = ArrayList<ImageModel>()
                    imageList.add(ImageModel(Utils.convertImageToBase64(requireActivity().contentResolver,imageUri!!).replace("\n","").replace(" ",""),))

                    // current date and time
                    val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                    Utils.endTime = currentTime.format(calendar.time)

                    val visitDistance = Utils.getDistance(
                        (outletData?.gio_lat ?: "0.0").toDouble(),
                        (outletData?.gio_long ?: "0.0").toDouble(),
                        (Utils.gio_lat ?: "0.0").toDouble(),
                        (Utils.gio_long ?: "0.0").toDouble()
                    )
                    val isException = when { visitDistance <= 100.00-> "0" else-> "1" }

                    visitViewModel.insertVisitData(
                        ScheduleVisit(
                            id = System.currentTimeMillis(),
                            schedule_id = Utils.currentSchedule?.schedule_id,
                            outlet_id = Utils.currentSchedule?.outlet_id,
                            outlet_type_id = outletData?.type_id,
                            outlet_channel_id = outletData?.channel_id,
                            visit_date = currentDate.format(calendar.time),
                            visit_time = Utils.currentSchedule?.schedule_time,
                            country_id = outletData?.country_id!!,
                            state_id = outletData?.state_id,
                            region_id = outletData?.region_id,
                            location_id = outletData?.location_id,
                            visit_type = 1,
                            stat_time = Utils.startTime,
                            end_time = Utils.endTime,
                            gio_lat = Utils.gio_lat?:"0.0",
                            gio_long = Utils.gio_long?:"0.0",
                            is_exception = isException,
                            visit_distance = visitDistance.toString(),
                            isInternetAvailable = Utils.checkForInternet(requireActivity()),
                            image_list = Gson().toJson(imageList),
                            notes = binding.notes.text.toString()
                        ))
                }
            }
        }


        // create visit response
        visitViewModel.insertVisitResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1) {
                    val scheduleItem = Utils.currentSchedule
                    scheduleItem.outlet_visit = 1
                    scheduleItem.visit_status = 1
                    scheduleItem.merchandising_visit = 1
                    dashboardViewModel.updateScheduleData(scheduleItem)
                } else {
                    showErrorToast(requireContext(),"Failed to visit store")
                }
            } else {
                showErrorToast(requireContext(),"Something went wrong !")
            }
        }

        dashboardViewModel.updateSchedule.observe(viewLifecycleOwner){
            if (it!=null){
                if (it.toInt() != -1) {
                    dashboardViewModel.getDashboardData()
                } else {
                    showErrorToast(requireContext(),"Failed to visit store")
                }
            } else {
                showErrorToast(requireContext(),"Something went wrong !")
            }
        }

        dashboardViewModel.dashboardData.observe(viewLifecycleOwner) {
            if (it != null) {
                val dashboard = it
                dashboard.visited_sales = (dashboard.visited_sales?:0)+1
                dashboardViewModel.updateDashboardData(dashboard)
                showSuccessToast(requireContext(),"Store visit successfully added")
                findNavController().navigate(R.id.dashboardFragment)
            } else {
                showErrorToast(requireContext(),"Something went wrong !")
                findNavController().navigate(R.id.dashboardFragment)
            }
        }


        visitViewModel.loading.observe(viewLifecycleOwner) {
            if (it != null) {
                when { it-> ProgressLoader.show() else-> ProgressLoader.dismiss() }
            }
        }


        return binding.root
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    imageUri = fileUri
                    binding.image.setImageURI(fileUri)
                    Utils.convertImageToBase64(requireActivity().contentResolver,fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireActivity(),ImagePicker.getError(data),Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
}