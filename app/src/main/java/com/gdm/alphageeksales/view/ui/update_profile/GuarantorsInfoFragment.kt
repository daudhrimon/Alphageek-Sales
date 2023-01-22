package com.gdm.alphageeksales.view.ui.update_profile

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.remote.document_type.DocumentType
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.data.remote.update.UpdateProfileData
import com.gdm.alphageeksales.databinding.FragmentGuarantorsInfoBinding
import com.gdm.alphageeksales.utils.Communicator
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.viewmodels.InformationViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuarantorsInfoFragment : Fragment() {
    private lateinit var binding: FragmentGuarantorsInfoBinding
    private lateinit var communicator: Communicator
    private var documentUri: Uri? = null
    private var documentList: ArrayList<DocumentType> = ArrayList()
    private lateinit var documentID:String
    private val viewModel: InformationViewModel by viewModels()
    private var profileData: ProfileData? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuarantorsInfoBinding.inflate(layoutInflater)
        communicator = requireActivity() as Communicator
        SharedPref.init(requireContext())
        val update = (SharedPref.read("UP_PROFILE", "") ?: "") == "YES"

        if (update){
            profileData = Gson().fromJson(SharedPref.read("PROFILE","") ?: "", ProfileData::class.java)
            profileData?.guarantor?.guarantor_name?.let { if (it.isNotEmpty()) { binding.guarantorName.setText(it) } }
            profileData?.guarantor?.guarantor_phone?.let { if (it.isNotEmpty()) { binding.guarantorPhone.setText(it) } }
            profileData?.guarantor?.guarantor_email?.let { if (it.isNotEmpty()) { binding.guarantorEmail.setText(it) } }
            profileData?.guarantor?.guarantor_id?.let { if (it.isNotEmpty()) {
                Picasso.get().load(it).error(R.drawable.ic_file).into(binding.documentImage)
            } }
        }


        binding.uploadDocument.setOnClickListener{
            ImagePicker.with(this)
                .crop()	    			                        //Crop image(Optional), Check Customization for more option
                .compress(1024)			                //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	        //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }


        // get document type list
        viewModel.getDocumentType()
        viewModel.documentTypeResponse.observe(requireActivity()){
            if (it != null) {
                if (isAdded) {
                    documentList.clear()
                    documentList.add(DocumentType(-1,"Guarantor Document Type"))
                    documentList.addAll(it.data)
                    binding.guarantorIdType.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,documentList)
                    if (update){
                        profileData?.guarantor?.guarantor_id_type?.let { gIdType-> if (gIdType.toString().isNotEmpty()) {
                            for (t in documentList.indices) {
                                when(documentList[t].id) { gIdType -> binding.guarantorIdType.setSelection(t) }
                            }
                        } }
                    }
                }
            }
        }

        // get selected bank
        binding.guarantorIdType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem == "Guarantor Document Type" || selectedItem == null) {/**/} else {
                    documentID = documentList[binding.guarantorIdType.selectedItemPosition].id.toString()
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }


        binding.nextBtn.setOnClickListener{
            val guarantorName   = binding.guarantorName.text.toString()
            val guarantorPhone  = binding.guarantorPhone.text.toString()
            val guarantorEmail  = binding.guarantorEmail.text.toString()
            val guarantorIDType = binding.guarantorIdType.selectedItem?.toString()

            when {
                guarantorName.isEmpty() -> {
                    binding.guarantorName.requestFocus()
                    binding.guarantorName.error = "Guarantor Name"
                }
                guarantorPhone.isEmpty() -> {
                    binding.guarantorPhone.requestFocus()
                    binding.guarantorPhone.error = "Phone Number"
                }
                guarantorPhone.length != 11 -> {
                    binding.guarantorPhone.requestFocus()
                    binding.guarantorPhone.error = "Mobile Number must be 11 digits"
                }
                !checkDigits(guarantorPhone) ->{
                    binding.guarantorPhone.requestFocus()
                    binding.guarantorPhone.error = "Invalid Phone Number"
                }
                guarantorEmail.isEmpty() -> {
                    binding.guarantorEmail.requestFocus()
                    binding.guarantorEmail.error = "Guarantor Email"
                }
                !guarantorEmail.matches(Utils.EMAIL_PATTERN) -> {
                    binding.guarantorEmail.requestFocus()
                    binding.guarantorEmail.error = "Invalid Email"
                }
                guarantorIDType == "Guarantor Document Type" || guarantorIDType == null -> {
                    showErrorToast(requireContext(),"Please select Guarantor ID Type")
                }
                documentUri == null && !update -> {
                    showErrorToast(requireContext(),"Please upload Guarantor ID")
                }
                else -> {
                    UpdateProfileData.guarantor_name          = guarantorName
                    UpdateProfileData.guarantor_phone         = guarantorPhone
                    UpdateProfileData.guarantor_email         = guarantorEmail
                    UpdateProfileData.guarantor_id_type       = documentID
                    UpdateProfileData.guarantor_document_uri  = documentUri
                    communicator.changeView()
                }
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
                    documentUri = fileUri
                    binding.documentImage.setImageURI(documentUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireActivity(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun checkDigits(number: String): Boolean {
        try {
            val fiveDigits = resources.getStringArray(R.array.prefix_five)
            val fourDigits = resources.getStringArray(R.array.prefix_four)
            val numberFive = number.substring(0, 5)
            val numberFour = number.substring(0, 4)
            var check = 0
            for (i in fiveDigits.indices) {
                if (numberFive.contains(fiveDigits[i])) {
                    check = 1
                }
            }
            for (i in fourDigits.indices) {
                if (numberFour.contains(fourDigits[i])) {
                    check = 1
                }
            }
            return check == 1

        }catch (e:Exception){
            return false
        }

    }
}