package com.gdm.alphageeksales.view.ui.update_profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.data.remote.update.UpdateProfileData
import com.gdm.alphageeksales.databinding.FragmentPictureCaptureBinding
import com.gdm.alphageeksales.utils.ProgressLoader
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.Utils.convertImageMultiPart
import com.gdm.alphageeksales.utils.Utils.convertMultiPart
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.utils.showSuccessToast
import com.gdm.alphageeksales.view.ui.auth.LoginActivity
import com.gdm.alphageeksales.viewmodels.ProfileViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PictureCaptureFragment : Fragment() {
    private lateinit var binding: FragmentPictureCaptureBinding
    private val viewModel: ProfileViewModel by viewModels()
    private  var mProfileUri:Uri? = null
    private var profileData: ProfileData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPictureCaptureBinding.inflate(layoutInflater)
        ProgressLoader.init(requireActivity())
        SharedPref.init(requireContext())
        val update = (SharedPref.read("UP_PROFILE", "") ?: "") == "YES"

        if (update) {
            profileData = Gson().fromJson(SharedPref.read("PROFILE","") ?: "", ProfileData::class.java)
            profileData?.details?.image?.let { image->
                if (image.isNotEmpty()) {
                    Picasso.get().load(image).error(R.drawable.ic_file).into(binding.userImage)
                }
            }
        }

        binding.userImage.setOnClickListener{
            ImagePicker.with(this)
                .crop(1f,1f)	    			                //Crop image(Optional), Check Customization for more option
                .compress(1024)			        //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.confirmBtn.setOnClickListener{
            if (mProfileUri != null || update){
                profileUpdate()
            }else{
                showErrorToast(requireContext(),"Please upload your picture")
            }
        }

        viewModel.profileUpdateResponse.observe(requireActivity()){
            if (it != null) {
                if (isAdded) {
                    if (it.success){
                        showSuccessToast(requireContext(),it.message)
                        startActivity(Intent(requireActivity(),LoginActivity::class.java))
                        requireActivity().finish()
                    }else{
                        showErrorToast(requireContext(),it.message)
                    }
                }
            }
        }

        // handle error
        viewModel.errorMessage.observe(requireActivity()) {
            showErrorToast(requireContext(),it.toString())
        }

        viewModel.loading.observe(requireActivity()) {
            if (it) {
                ProgressLoader.show()
            } else {
                ProgressLoader.dismiss()
            }
        }


        return binding.root
    }

    private fun profileUpdate() {
        ProgressLoader.show()
        val documentMultiPart = UpdateProfileData.guarantor_document_uri?.let { convertImageMultiPart(it,"guarantor_id") }
        val userImageMultiPArt = mProfileUri?.let { convertImageMultiPart(it,"image") }
        viewModel.updateProfile(
            convertMultiPart(UpdateProfileData.firstName),
            convertMultiPart(UpdateProfileData.lastName),
            convertMultiPart(UpdateProfileData.middleName),
            convertMultiPart(UpdateProfileData.gender),
            convertMultiPart(UpdateProfileData.phone),
            convertMultiPart(UpdateProfileData.address),
            convertMultiPart(UpdateProfileData.countryID),
            convertMultiPart(UpdateProfileData.stateID),
            convertMultiPart(UpdateProfileData.lga_id),
            convertMultiPart(UpdateProfileData.nin),
            convertMultiPart(UpdateProfileData.bvn),
            convertMultiPart(UpdateProfileData.lasra),
            convertMultiPart(UpdateProfileData.education),
            convertMultiPart(UpdateProfileData.bank_id),
            convertMultiPart(UpdateProfileData.account_name),
            convertMultiPart(UpdateProfileData.account_number),
            convertMultiPart(UpdateProfileData.guarantor_name),
            convertMultiPart(UpdateProfileData.guarantor_email),
            convertMultiPart(UpdateProfileData.guarantor_phone),
            convertMultiPart(UpdateProfileData.guarantor_id_type),
            documentMultiPart, userImageMultiPArt
        )
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    mProfileUri = fileUri
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