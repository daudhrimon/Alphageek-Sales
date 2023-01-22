package com.gdm.alphageeksales.view.ui.module.inventory

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Inventory
import com.gdm.alphageeksales.data.local.down_sync.Product
import com.gdm.alphageeksales.data.remote.InventoryRequest
import com.gdm.alphageeksales.databinding.DialogGetInventoryBinding
import com.gdm.alphageeksales.databinding.FragmentInventoryManageBinding
import com.gdm.alphageeksales.utils.*
import com.gdm.alphageeksales.view.adapter.GetInventoryAdapter
import com.gdm.alphageeksales.view.adapter.InventoryAdapter
import com.gdm.alphageeksales.viewmodels.AuthViewModel
import com.gdm.alphageeksales.viewmodels.ProductViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class InventoryManageFragment : Fragment() {
    private lateinit var binding: FragmentInventoryManageBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private var requestList = ArrayList<Product>()
    private var inventoryList = ArrayList<Inventory>()
    private var getInventoryAdapter: GetInventoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInventoryManageBinding.inflate(layoutInflater)
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        ProgressLoader.init(requireContext())

        productViewModel.getInventoryList()
        productViewModel.inventoryList.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                inventoryList.clear()
                inventoryList.addAll(it)
                binding.emptyLayout.isGONE()
                binding.inventoryLayout.isVISIBLE()
                binding.recyclerview.adapter = InventoryAdapter(inventoryList)
            } else {
                binding.inventoryLayout.isGONE()
                binding.emptyLayout.isVISIBLE()
            }
        }

        productViewModel.getAllProducts()
        productViewModel.productListAll.observe(viewLifecycleOwner) {
            if (it != null) {
                requestList.clear()
                requestList.addAll(it)
                println(it)
            }
        }

        binding.searchBtn.setOnClickListener {
            if (binding.searchBar.visibility == View.GONE) {
                binding.btnLayout.isGONE()
                binding.searchBar.isVISIBLE()
                binding.searchBar.requestFocus()
                Keyboard.showKeyboard(binding.searchBar)
                binding.searchBtn.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_close_24)
            } else {
                Keyboard.hideSoftKeyBoard(requireContext(),binding.searchBar)
                binding.searchBar.isGONE()
                binding.btnLayout.isVISIBLE()
                binding.searchBtn.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_search_24)
                binding.searchBar.setText("")
            }
        }

        binding.pendingRequest.setOnClickListener {
            findNavController().navigate(R.id.pendingRequestFragment)
        }

        binding.getInventory.setOnClickListener {
            productViewModel.productList.postValue(ArrayList())
            showGetInventoryDialog()
        }

        binding.searchBar.doAfterTextChanged {
            if (it != null && it.toString().isNotEmpty()) {
                val filteredList = filterInventoryList(inventoryList,it.toString())
                binding.recyclerview.adapter = InventoryAdapter(filteredList)
            } else {
                binding.recyclerview.adapter = InventoryAdapter(inventoryList)
            }
        }

        //inventory response
        authViewModel.getInventoryResponse.observe(viewLifecycleOwner) {
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

    private fun showGetInventoryDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBind = DialogGetInventoryBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBind.root)

        getInventoryAdapter = GetInventoryAdapter(requestList)
        dialogBind.recyclerview.adapter = getInventoryAdapter

        dialogBind.saveBtn.setOnClickListener {
            if (Utils.checkForInternet(requireContext())) {
                val requestedList = ArrayList<InventoryRequest>()
                getInventoryAdapter?.getList()?.forEach {
                    if (it.case_qty != null && it.unit_qty != null) {
                        if ((it.case_qty?:0)>0 || (it.unit_qty?:0)>0) {
                            requestedList.add(InventoryRequest(
                                product_id = it.id,
                                brand_id = it.brand_id,
                                client_id = it.client_id,
                                category_id = it.category_id,
                                sales_price = (it.sales_price?:0).toInt(),
                                case_qty  = it.case_qty,
                                unit_qty = it.unit_qty,
                                unit_price = it.unit_price!!.toInt()
                            ))
                        }
                    }
                }.also {
                    if (requestedList.isNotEmpty()) {
                        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    ProgressLoader.show()
                                    authViewModel.inventoryRequest(Gson().toJson(requestedList))
                                    dialog.dismiss()
                                    dialog.dismiss()
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    dialog.dismiss()
                                }
                            }
                        }
                        val builder = AlertDialog.Builder(requireContext(),R.style.Calender_dialog_theme)
                        builder.setTitle(getString(R.string.inventory_request))
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show()
                    } else {
                        showErrorToast(requireContext(),"Please select product first")
                    }
                }
            } else {
                showErrorToast(requireContext(),"No internet connection")
            }
            // update schedule table
        }

        dialogBind.btnCancle.setOnClickListener {
            dialog.dismiss()
        }

        dialogBind.closeBtn.setOnClickListener {
            Keyboard.hideSoftKeyBoard(dialog.context,dialogBind.searchBar)
            dialogBind.searchBar.apply {
                setText("")
                clearFocus()
            }
        }

        dialogBind.searchBar.doAfterTextChanged {
            if (it != null && it.toString().isNotEmpty()) {
                val filteredList = filterRequestList(requestList,it.toString())
                dialogBind.recyclerview.adapter = GetInventoryAdapter(filteredList)
            } else {
                getInventoryAdapter = GetInventoryAdapter(requestList)
                dialogBind.recyclerview.adapter = getInventoryAdapter
            }
        }

        dialog.setCancelable(false)
        dialog.show()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        dialog.window?.setLayout((9 * width) / 10,(19 * height) / 20)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    private fun filterRequestList(requestList: ArrayList<Product>, text: String): ArrayList<Product> {
        val filteredList = ArrayList<Product>()
        val iterator = requestList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next() as Product
            if (item.product_name.toString().lowercase().contains(text.lowercase())) {
                filteredList.add(item)
            }
        }
        return filteredList
    }

    private fun filterInventoryList(inventoryList: ArrayList<Inventory>, text: String): ArrayList<Inventory> {
        val filterList = ArrayList<Inventory>()
        val iterator: Iterator<*> = inventoryList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next() as Inventory
            if (item.product_name.toString().lowercase().contains(text.lowercase())) {
                filterList.add(item)
            }
        }
        return filterList
    }
}