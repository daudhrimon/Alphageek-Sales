package com.gdm.alphageeksales.view.ui.update_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.gdm.alphageeksales.data.remote.bank.BankData
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.data.remote.update.UpdateProfileData
import com.gdm.alphageeksales.databinding.FragmentBankAccountInfoBinding
import com.gdm.alphageeksales.utils.Communicator
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.viewmodels.InformationViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BankAccountInfoFragment : Fragment() {
    lateinit var binding:FragmentBankAccountInfoBinding
    lateinit var communicator: Communicator
    private var bankList: ArrayList<BankData> = ArrayList()
    private lateinit var bankID:String
    private val viewModel: InformationViewModel by viewModels()
    private var profileData: ProfileData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBankAccountInfoBinding.inflate(layoutInflater)
        communicator = requireActivity() as Communicator
        SharedPref.init(requireContext())
        val update = (SharedPref.read("UP_PROFILE", "") ?: "") == "YES"

        if (update) {
            profileData = Gson().fromJson(SharedPref.read("PROFILE","") ?: "", ProfileData::class.java)
            profileData?.account?.account_name?.let { if (it.isNotEmpty()) { binding.accountName.setText(it) } }
            profileData?.account?.account_number?.let { if (it.isNotEmpty()) { binding.accountNumber.setText(it) } }
        }


        // get BankList
        viewModel.getBankList()
        viewModel.bankResponse.observe(requireActivity()){
            if (it != null) {
                if (isAdded) {
                    bankList.clear()
                    bankList.addAll(it.data)
                    binding.bank.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bankList)
                    if (update) {
                        profileData?.account?.bank_id?.let {bankId-> if (bankId.toString().isNotEmpty()) {
                            for (b in bankList.indices) {
                                when(bankList[b].id) { bankId-> binding.bank.setSelection(b) }
                            }
                        } }
                    }
                }
            }
        }


        // get selected bank
        binding.bank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (selectedItem == "Select Bank" || selectedItem == null) {/**/} else {
                    bankID = bankList[binding.bank.selectedItemPosition].id.toString()
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {/**/}
        }


        binding.nextBtn.setOnClickListener{
            val bank          = binding.bank.selectedItem?.toString()
            val accountName   = binding.accountName.text.toString()
            val accountNumber = binding.accountNumber.text.toString()

            when {
                bank == "Select Bank" || bank == null-> {
                    showErrorToast(requireContext(),"Please select Bank")
                }
                accountName.isEmpty() -> {
                    binding.accountName.requestFocus()
                    binding.accountName.error = "Account Name"
                }
                accountNumber.isEmpty() -> {
                    binding.accountNumber.requestFocus()
                    binding.accountNumber.error = "Account Number"
                }

                accountNumber.length<10 -> {
                    binding.accountNumber.requestFocus()
                    binding.accountNumber.error = "Account Number Is Invalid"
                }
                else -> {
                    UpdateProfileData.bank_id        = bankID
                    UpdateProfileData.account_name   = accountName
                    UpdateProfileData.account_number = accountNumber
                    communicator.changeView()
                }
            }
        }

        return binding.root
    }
}