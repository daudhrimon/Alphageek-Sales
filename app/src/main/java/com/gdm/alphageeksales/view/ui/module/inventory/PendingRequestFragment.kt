package com.gdm.alphageeksales.view.ui.module.inventory

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Checkout
import com.gdm.alphageeksales.databinding.FragmentPendingRequestBinding
import com.gdm.alphageeksales.utils.ProgressLoader
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.utils.showSuccessToast
import com.gdm.alphageeksales.view.adapter.CheckOutMainAdapter
import com.gdm.alphageeksales.viewmodels.AuthViewModel
import com.gdm.alphageeksales.viewmodels.ProductViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PendingRequestFragment : Fragment() {
    private lateinit var binding: FragmentPendingRequestBinding
    private val productViewModel: ProductViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPendingRequestBinding.inflate(layoutInflater)
        ProgressLoader.init(requireContext())

        productViewModel.getCheckOUtList()
        productViewModel.checkOutList.observe(viewLifecycleOwner) {
            if (it != null) {
                checkOUtRecyclerView(ArrayList(it.asReversed()))
            }
        }

        authViewModel.getInventoryResponse.observe(viewLifecycleOwner) {
            ProgressLoader.dismiss()
            if (it.success) {
                showSuccessToast(requireContext(),it.message)
            } else {
                showErrorToast(requireContext(),it.message)
            }
        }

        // handle error
        authViewModel.errorMessage.observe(viewLifecycleOwner) {
            showErrorToast(requireContext(),it.toString())
        }

        authViewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                ProgressLoader.show()
            } else {
                ProgressLoader.dismiss()
            }
        }

        //start sazzad
        authViewModel.checkoutResponse.observe(viewLifecycleOwner) {
            ProgressLoader.dismiss()
            if (it.success) {
                Utils.haveToSync = true
                showSuccessToast(requireContext(),it.message)
                requireActivity().finish()
            } else {
                showErrorToast(requireContext(),it.message)
            }
        }

        // handle error
        authViewModel.errorMessage.observe(viewLifecycleOwner) {
            showErrorToast(requireContext(),it.toString())
        }

        authViewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                ProgressLoader.show()
            } else {
                ProgressLoader.dismiss()
            }
        }


        return binding.root
    }


    private fun checkOUtRecyclerView(checkouts: ArrayList<Checkout>) {
        val adapter = CheckOutMainAdapter(checkouts, requireContext())
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerview.adapter = adapter

        binding.approveRequestBtn.setOnClickListener {
            if (Utils.checkForInternet(requireContext())) {
                if (adapter?.getList() != null && adapter?.getList().isNotEmpty()) {
                    val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                ProgressLoader.show()
                                val jsonArray = JsonArray()
                                for (item in adapter.getList()) {
                                    val jo= JsonObject()
                                    jo.addProperty("checkout_id",item)
                                    jsonArray.add(jo)
                                    Log.i("checkoutReq", item)
                                }
                                authViewModel.checkOutRequest(jsonArray.toString())
                                dialog.dismiss()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                                dialog.dismiss()
                            }
                        }
                    }
                    val builder = AlertDialog.Builder(requireContext(),R.style.Calender_dialog_theme)
                    builder.setTitle(getString(R.string.checkoutt))
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show()
                } else {
                    showErrorToast(requireContext(),"Please select product first")
                }
            } else {
                showErrorToast(requireContext(),"No internet connection")
            }
        }
    }
}