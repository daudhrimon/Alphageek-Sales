package com.gdm.alphageeksales.view.ui.update_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.remote.education.EducationData
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.data.remote.update.UpdateProfileData
import com.gdm.alphageeksales.databinding.FragmentPersonalInfoBinding
import com.gdm.alphageeksales.utils.Communicator
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.viewmodels.InformationViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalInfoFragment : Fragment() {
    private lateinit var binding:FragmentPersonalInfoBinding
    private val informationViewModel: InformationViewModel by viewModels()
    private lateinit var communicator: Communicator
    private var educationList: ArrayList<EducationData> = ArrayList()
    private lateinit var educationID:String
    private var profileData: ProfileData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalInfoBinding.inflate(layoutInflater)
        communicator = requireActivity() as Communicator
        SharedPref.init(requireContext())
        val update = (SharedPref.read("UP_PROFILE", "") ?: "") == "YES"

        if (update) {
            profileData = Gson().fromJson(SharedPref.read("PROFILE","") ?: "", ProfileData::class.java)
            profileData?.details?.let { details->
                details.firstname?.let { when{it.isNotEmpty()-> binding.firstName.setText(it)} }
                details.middlename?.let { when{it.isNotEmpty()-> binding.middleName.setText(it)} }
                details.lastname?.let { when{it.isNotEmpty()-> binding.lastName.setText(it)} }
                details.address?.let { when{it.isNotEmpty()-> binding.address.setText(it)} }
                details.phone?.let { when{it.isNotEmpty()-> binding.phone.setText(it)} }
                details.gender?.let {
                    when {
                        it.isNotEmpty() && it == "Male" -> binding.gender.setSelection(1)
                        it.isNotEmpty() && it == "Female" -> binding.gender.setSelection(2)
                    }
                }
            }
        }


        // get education list
        informationViewModel.getEducationList()
        informationViewModel.educationResponse.observe(viewLifecycleOwner){
            if (it != null) {
                educationList.clear()
                educationList.addAll(it.data)
                binding.education.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,educationList)
                if (update) {
                    profileData?.details?.education?.let { education->
                        if (education.isNotEmpty()) {
                            for (e in educationList.indices) {
                                when (educationList[e].title) { education -> binding.education.setSelection(e) }
                            }
                        }
                    }
                }
            }
        }


        // get selected education id
        binding.education.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                try {
                    val selectedItem = adapterView?.selectedItem?.toString()
                    if (selectedItem == "Select LGA" || selectedItem == null) {/**/} else {
                        educationID = educationList[binding.education.selectedItemPosition].id.toString()
                    }
                }catch (e:Exception){
                    Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }


        binding.nextBtn.setOnClickListener{

            val firstName    = binding.firstName.text.toString()
            val middleName   = binding.middleName.text.toString()
            val lastName     = binding.lastName.text.toString()
            val homeAddress  = binding.address.text.toString()
            val phoneNumber  = binding.phone.text.toString()
            val gender       = binding.gender.selectedItem.toString()
            val education    = binding.education.selectedItem?.toString()

            when {
                firstName.isEmpty() -> {
                    binding.firstName.requestFocus()
                    binding.firstName.error = "First Name"
                }
                lastName.isEmpty() -> {
                    binding.lastName.requestFocus()
                    binding.lastName.error = "Last Name"
                }
                homeAddress.isEmpty() -> {
                    binding.address.requestFocus()
                    binding.address.error = "Address"
                }
                phoneNumber.isEmpty() -> {
                    binding.phone.requestFocus()
                    binding.phone.error = "Number"
                }
                phoneNumber.length != 11 -> {
                    binding.phone.requestFocus()
                    binding.phone.error = "Mobile Number must be 11 digits"
                }
                !checkDigits(phoneNumber) ->{
                    binding.phone.requestFocus()
                    binding.phone.error = "Invalid Phone Number"
                }
                gender == "Select Gender" -> {
                    showErrorToast(requireContext(),"Please select gender")
                }
                education == "Education" || education == null -> {
                    showErrorToast(requireContext(),"Please select Education")
                }
                else -> {
                    UpdateProfileData.firstName  = firstName
                    UpdateProfileData.middleName = middleName
                    UpdateProfileData.lastName   = lastName
                    UpdateProfileData.address    = homeAddress
                    UpdateProfileData.phone      = phoneNumber
                    UpdateProfileData.gender     = if (gender == "Male") "1" else "2"
                    UpdateProfileData.education  = educationID
                    communicator.changeView()
                }
            }

        }


        return binding.root
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

        } catch (e:Exception) {
            return false
        }
    }
}