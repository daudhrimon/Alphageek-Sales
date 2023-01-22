package com.gdm.alphageeksales.view.ui.update_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gdm.alphageeksales.data.remote.country.CountryData
import com.gdm.alphageeksales.data.remote.lga.LgaData
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.data.remote.state.StateData
import com.gdm.alphageeksales.data.remote.update.UpdateProfileData
import com.gdm.alphageeksales.databinding.FragmentUniqueIdentificationBinding
import com.gdm.alphageeksales.utils.Communicator
import com.gdm.alphageeksales.utils.ProgressLoader
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.viewmodels.InformationViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UniqueIdentificationFragment : Fragment() {
    private lateinit var binding: FragmentUniqueIdentificationBinding
    private lateinit var communicator: Communicator
    private var countryList: ArrayList<CountryData> = ArrayList()
    private var stateList: ArrayList<StateData> = ArrayList()
    private var lgaList: ArrayList<LgaData> = ArrayList()
    private lateinit var countryID:String
    private lateinit var stateID:String
    private lateinit var lgaID:String
    private val viewModel: InformationViewModel by viewModels()
    private var profileData: ProfileData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUniqueIdentificationBinding.inflate(layoutInflater)
        communicator = requireActivity() as Communicator
        SharedPref.init(requireContext())
        val update = (SharedPref.read("UP_PROFILE", "") ?: "") == "YES"

        if (update) {
            profileData = Gson().fromJson(SharedPref.read("PROFILE","") ?: "", ProfileData::class.java)
            profileData?.details?.let { details->
                details.nin?.let { when{it.isNotEmpty()-> binding.ninNumber.setText(it)} }
                details.bvn?.let { when{it.isNotEmpty()-> binding.bvnNumber.setText(it)} }
                details.lassra?.let { when{it.isNotEmpty()-> binding.lassra.setText(it)} }
            }
        }


        // get country list
        viewModel.getCountryList()
        viewModel.countryListResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                countryList.clear()
                countryList.add(CountryData("Select Country", -1))
                countryList.addAll(it.data)
                binding.country.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, countryList)
                if (update) {
                    profileData?.details?.country_id?.let { country_id->
                        if (country_id.isNotEmpty()) {
                            for ( c in countryList.indices) {
                                when(countryList[c].country_name) { country_id -> binding.country.setSelection(c) }
                            }
                        }
                    }
                }
            }
        }

        // get state list according to country
        viewModel.stateListResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                stateList.clear()
                stateList.add(StateData(-1, -1, "Select State"))
                stateList.addAll(it.data)
                binding.state.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, stateList)
                if (update) {
                    profileData?.details?.state_id?.let { state_id->
                        if (state_id.isNotEmpty()) {
                            for ( s in stateList.indices) {
                                when(stateList[s].state_name) { state_id -> binding.state.setSelection(s) }
                            }
                        }
                    }
                }
            }
        }

        // get state list according to state
        viewModel.lgaResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                lgaList.clear()
                lgaList.add(LgaData(-1, "Select LGA"))
                lgaList.addAll(it.data)
                binding.lga.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,lgaList)
                if (update) {
                    profileData?.details?.lga?.let { lga->
                        if (lga.isNotEmpty()) {
                            for (l in lgaList.indices) {
                                when (lgaList[l].location_name) { lga -> binding.lga.setSelection(l) }
                            }
                        }
                    }
                }
            }
        }

        // get selected country
        binding.country.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem == "Select Country" || selectedItem == null) {/**/} else {
                    countryID = countryList[binding.country.selectedItemPosition].id.toString()
                    viewModel.getStateList(countryID)
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        // get selected state
        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem == "Select State" || selectedItem == null) {/**/} else {
                    stateID = stateList[binding.state.selectedItemPosition].id.toString()
                    viewModel.getLgaList(stateID)
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        // get selected lga
        binding.lga.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem == "Select LGA" || selectedItem == null) {/**/} else {
                    lgaID = lgaList[binding.lga.selectedItemPosition].id.toString()
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }



        binding.nextBtn.setOnClickListener {
            val country = binding.country.selectedItem?.toString()
            val state = binding.state.selectedItem?.toString()
            val lga = binding.lga.selectedItem?.toString()
            val ninNumber = binding.ninNumber.text.toString()
            val bvnNumber = binding.bvnNumber.text.toString()
            val lassraNumber = binding.lassra.text.toString()

            when {
                country == "Select Country" || country == null -> {
                    showErrorToast(requireContext(),"Please select Country")
                }
                state == "Select State" || state == null -> {
                    showErrorToast(requireContext(),"Please select State")
                }
                lga == "Select LGA" || lga == null -> {
                    showErrorToast(requireContext(),"Please select LGA")
                }
                ninNumber.isEmpty() && bvnNumber.isEmpty() && lassraNumber.isEmpty() -> {
                    showErrorToast(requireContext(),"Please provide any info between NIN, BVN & LASSRA")
                }
                bvnNumber.isNotEmpty() && bvnNumber.length != 11 -> {
                    binding.bvnNumber.requestFocus()
                    binding.bvnNumber.error = "BVN Number must be 11 digits"
                }
                else -> {
                    UpdateProfileData.countryID  = countryID
                    UpdateProfileData.stateID    = stateID
                    UpdateProfileData.lga_id     = lgaID
                    UpdateProfileData.nin        = ninNumber
                    UpdateProfileData.bvn        = bvnNumber
                    UpdateProfileData.lasra      = lassraNumber
                    communicator.changeView()
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
}