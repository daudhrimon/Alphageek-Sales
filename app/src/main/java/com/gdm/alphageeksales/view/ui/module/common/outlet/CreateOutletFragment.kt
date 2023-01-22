package com.gdm.alphageeksales.view.ui.module.common.outlet

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Base64.decode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageeksales.MainActivity
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.databinding.FragmentCreateOutletBinding
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.Utils.convertImageToBase64
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.utils.showSuccessToast
import com.gdm.alphageeksales.view.adapter.QuestionsAdapter
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import com.gdm.alphageeksales.viewmodels.OutletViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateOutletFragment : Fragment() {
    private lateinit var binding: FragmentCreateOutletBinding
    private lateinit var locationDownSync: LocationDownSync
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val outletViewModel: OutletViewModel by viewModels()
    private val locationList = ArrayList<LocationDownSync>()
    private val outletTypesList = ArrayList<OutletType>()
    private val channelList = ArrayList<OutletChannel>()
    private var questionsAdapter: QuestionsAdapter? = null
    private var dashboard: Dashboard? = null
    private var outletUp: Outlet? = null
    private var imageUri: Uri? = null
    private var questionsCount = 0
    private var outletTypeID = 0
    private var channelID = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateOutletBinding.inflate(layoutInflater)

        try { outletUp = Gson().fromJson(requireArguments().getString("OUTLET",""),Outlet::class.java) } catch (e: Exception) {/**/}
        if (outletUp != null) {
            try { (activity as MainActivity).titleName.text = "Update Outlet" } catch (e: Exception){/**/}
            getOutletUpdateInfo()
        }

        // get outlet types , locations  channel value from db
        outletViewModel.getOutletTypes()
        outletViewModel.getOutletChannel()
        dashboardViewModel.getAllLocationList()

        // observe channel data
        outletViewModel.outletChannel.observe(viewLifecycleOwner) {
            if (it != null) {
                channelList.clear()
                channelList.add(OutletChannel(-1,"Outlet Channel"))
                channelList.addAll(it)
                binding.outletChannel.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,channelList)
                //  for update outlet
                outletUp?.channel_id?.let { channel_id ->
                    if (channel_id.toString().isNotEmpty()) {
                        for (c in channelList.indices) {
                            when(channelList[c].id) { channel_id -> binding.outletChannel.setSelection(c) }
                        }
                    }
                }
            }
        }

        // observe outlet type data
        outletViewModel.outletTypes.observe(viewLifecycleOwner) {
            if (it != null) {
                outletTypesList.clear()
                outletTypesList.add(OutletType(-1,"Outlet Type"))
                outletTypesList.addAll(it)
                binding.outletType.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,outletTypesList)
                //  for update outlet
                outletUp?.type_id?.let { type_id ->
                    if (type_id.toString().isNotEmpty()) {
                        for (t in outletTypesList.indices) {
                            when(outletTypesList[t].id) { type_id -> binding.outletType.setSelection(t) }
                        }
                    }
                }
            }
        }

        // observe location type data
        dashboardViewModel.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList.clear()
                locationList.add(LocationDownSync(-1,-1,-1,-1,-1,"Select Location","",""))
                locationList.addAll(it)
                binding.locationSpinner.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,locationList)
                //  for update outlet
                outletUp?.location_id?.let { location_id ->
                    if (location_id.toString().isNotEmpty()) {
                        for (l in locationList.indices) {
                            when(locationList[l].location_id) { location_id -> binding.locationSpinner.setSelection(l) }
                        }
                    }
                }
            }
        }

        outletViewModel.getQuestions()
        outletViewModel.questionsData.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                it.apply { questionsCount = size
                    outletUp?.answers?.let { answers->
                        forEach { question->
                            answers.forEach { answer-> if (question.id == answer?.question_id){question.ans = answer.ans}}
                        }
                    }
                }.also { questions ->
                    binding.questionsLayout.isVISIBLE()
                    questionsAdapter = QuestionsAdapter(questions,requireContext())
                    binding.questionsRecycler.adapter = questionsAdapter
                }
            }
        }

        binding.userImage.setOnClickListener{
            ImagePicker.with(this)
                .crop()	    			                //Crop image(Optional), Check Customization for more option
                .compress(1024)			        //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        // get selected channel
        binding.outletChannel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem != "Outlet Channel" && selectedItem != null) {
                    channelID = channelList[binding.outletChannel.selectedItemPosition].id
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        // get selected outlet type
        binding.outletType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem != "Outlet Type" && selectedItem != null) {
                    outletTypeID = outletTypesList[binding.outletType.selectedItemPosition].id
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        // get selected location
        binding.locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem != "Select Location" && selectedItem != null) {
                    locationDownSync = locationList[binding.locationSpinner.selectedItemPosition]
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        binding.saveBtn.setOnClickListener {
            var requiredLeft = 0
            when { questionsCount > 0-> {
                val answersList = questionsAdapter?.getAnswersList()
                answersList?.forEach { answer ->
                    if ((answer.is_required?:0)==1 && answer.ans.isNullOrEmpty()){requiredLeft ++}
                }
            } }
            val outLetType           = binding.outletType.selectedItem?.toString()
            val outLetChannel        = binding.outletChannel.selectedItem?.toString()
            val outletName           = binding.outletName.text.toString()
            val outletPhone          = binding.outletPhone.text.toString()
            val isSpecialOutlet      = if (binding.specialOutletCheckbox.isChecked) 1 else 0
            val streetNumber         = binding.streetNumber.text.toString()
            val streetName           = binding.streetName.text.toString()
            val locationSpinner      = binding.locationSpinner.selectedItem?.toString()
            val firstName            = binding.firstName.text.toString()
            val lastName             = binding.lastName.text.toString()
            val note                 = binding.etNote.text.toString()
            val personPhoneNumber    = binding.personPhoneNumber.text.toString()

            when {
                outLetType == "Outlet Type" || outLetType == null-> {
                    showErrorToast(requireContext(),"Please select Outlet Type")
                }
                outLetChannel == "Outlet Channel" || outLetChannel == null-> {
                    showErrorToast(requireContext(),"Please select Outlet Channel")
                }
                outletName.isEmpty() -> {
                    binding.outletName.requestFocus()
                    binding.outletName.error = "Outlet Name"
                }
                outletPhone.isEmpty() -> {
                    binding.outletPhone.requestFocus()
                    binding.outletPhone.error = "Phone Number"
                }

                outletPhone.length!=11 -> {
                    binding.outletPhone.requestFocus()
                    binding.outletPhone.error = "Number should be 11 digit"
                }
                streetNumber.isEmpty() -> {
                    binding.streetNumber.requestFocus()
                    binding.streetNumber.error = "Street Number"
                }
                streetName.isEmpty() -> {
                    binding.streetName.requestFocus()
                    binding.streetName.error = "Street Name"
                }
                locationSpinner == "Select Location" || locationSpinner == null-> {
                    showErrorToast(requireContext(),"Please select Location")
                }
                firstName.isEmpty() -> {
                    binding.firstName.requestFocus()
                    binding.firstName.error = "First Name"
                }
                lastName.isEmpty() -> {
                    binding.lastName.requestFocus()
                    binding.lastName.error = "Last Name"
                }
                personPhoneNumber.isEmpty() -> {
                    binding.personPhoneNumber.requestFocus()
                    binding.personPhoneNumber.error = "Phone Number"
                }
                personPhoneNumber.length!=11 -> {
                    binding.personPhoneNumber.requestFocus()
                    binding.personPhoneNumber.error = "Number should be 11 digit"
                }
                requiredLeft > 0-> {
                    showErrorToast(requireContext(),"$requiredLeft Required ${
                        when(requiredLeft){ 1-> "question" else-> "questions" }
                    } left !")
                }
                imageUri == null && outletUp == null -> {
                    showErrorToast(requireContext(),"Please upload outlet image")
                }
                else -> {
                    val answers = arrayListOf<Answers>().apply {
                        questionsAdapter?.getAnswersList()?.let { answersList->
                            answersList.forEach {
                                it.ans?.let { ans-> when{ans.isNotEmpty()-> add(Answers(it.id,it.ans))} }
                            }
                        }
                    }
                    val image: String? = when { outletUp != null -> {
                        when { imageUri != null -> convertImageToBase64(requireActivity().contentResolver,imageUri!!)
                            else -> outletUp!!.outlet_image.toString()
                        }}
                        else -> imageUri?.let { it1 -> convertImageToBase64(requireActivity().contentResolver,it1) }
                    }
                    val outlet = Outlet(
                        System.currentTimeMillis(),
                        channelID,
                        Utils.gio_lat,
                        Utils.gio_long,
                        "$streetNumber,$streetName",
                        image?.replace("\n","")?.replace(" ",""),
                        outletName,
                        outletPhone,
                        locationDownSync.country_id,
                        locationDownSync.state_id,
                        locationDownSync.region_id,
                        locationDownSync.location_id,
                        locationDownSync.location_name,
                        outletTypeID,
                        firstName,
                        lastName,
                        streetName,
                        streetNumber,
                        personPhoneNumber,
                        isSpecialOutlet,
                        1,
                        0,
                        note,
                        answers
                    )
                    when {
                        image == null || image?.isEmpty() -> {
                            showErrorToast(requireContext(),"Please upload outlet image")
                        }
                        outletUp != null -> {
                            outlet.is_local = outletUp!!.is_local
                            outlet.outlet_id = outletUp!!.outlet_id
                            when (outletUp!!.is_local) { 0 -> outlet.is_up = 1 }
                            outletViewModel.updateOutlet(outlet)
                        }
                        else -> {
                            outletViewModel.insertNewOutlet(outlet)
                        }
                    }
                }

            }
        }

        dashboardViewModel.getDashboardData()
        dashboardViewModel.dashboardData.observe(viewLifecycleOwner) {
            if (it != null) {
                dashboard = it
            }
        }

        // create outlet response
        outletViewModel.insertOutlet.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.toInt() != -1){
                    showSuccessToast(requireContext(),"Outlet created successfully")
                    dashboard?.outlets = (dashboard?.outlets ?: 0) + 1
                    dashboard?.let { it1 -> dashboardViewModel.updateDashboardData(it1) }
                    findNavController().navigate(R.id.dashboardFragment)
                }else{
                    showErrorToast(requireContext(),"Failed to create outlet")
                }
            }
        }

        // create outlet response
        outletViewModel.updateOutlet.observe(viewLifecycleOwner) {
            if (it != null) {
                showSuccessToast(requireContext(),"Outlet updated successfully")
                findNavController().navigate(R.id.outletListFragment)
            } else {
                showErrorToast(requireContext(),"Failed to update outlet")
            }
        }


        return binding.root
    }

    private fun getOutletUpdateInfo() {
        outletUp!!.outlet_name?.let { when{it.isNotEmpty()-> binding.outletName.setText(it)} }
        outletUp!!.outlet_phone?.let { when{it.isNotEmpty()-> binding.outletPhone.setText(it)} }
        outletUp!!.note?.let { when{it.isNotEmpty()-> binding.etNote.setText(it)} }
        outletUp!!.street_no?.let { when{it.isNotEmpty()-> binding.streetNumber.setText(it)} }
        outletUp!!.street_name?.let { when{it.isNotEmpty()-> binding.streetName.setText(it)} }
        outletUp!!.cpf_name?.let { when{it.isNotEmpty()-> binding.firstName.setText(it)} }
        outletUp!!.cpl_name?.let { when{it.isNotEmpty()-> binding.lastName.setText(it)} }
        outletUp!!.cpp?.let { when{it.isNotEmpty()-> binding.personPhoneNumber.setText(it)} }
        outletUp!!.outlet_image?.let {
            if (it.isNotEmpty()) {
                val encodedString = "data:image/jpg;base64,"
                if (it.contains(encodedString)) {
                    val pureBase64Encoded = it.substring(encodedString.indexOf(",")  + 1)
                    val imageBytes = decode(pureBase64Encoded, Base64.DEFAULT)
                    val bitmapImg = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    try { binding.userImage.setImageBitmap(bitmapImg) } catch (e: Exception) {/**/}
                } else { Picasso.get().load(it).error(R.drawable.ic_camera_shape).into(binding.userImage) }
            }
        }
        binding.saveBtn.text = "Update"
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
                    binding.userImage.setImageURI(fileUri)

                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireActivity(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
}