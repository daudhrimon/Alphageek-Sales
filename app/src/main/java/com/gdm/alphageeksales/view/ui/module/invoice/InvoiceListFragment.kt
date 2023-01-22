package com.gdm.alphageeksales.view.ui.module.invoice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Order
import com.gdm.alphageeksales.databinding.FragmentInvoiceListBinding
import com.gdm.alphageeksales.utils.*
import com.gdm.alphageeksales.view.adapter.InvoiceListAdapter
import com.gdm.alphageeksales.viewmodels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceListFragment : Fragment(), InvoiceListItemClickListener {
    private lateinit var binding: FragmentInvoiceListBinding
    private var orderList = ArrayList<Order>()
    private val productViewModel: ProductViewModel by viewModels()
    private var haveBluetoothAccess = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInvoiceListBinding.inflate(layoutInflater)
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        //ProgressLoader.init(requireContext())

        if (ContextCompat.checkSelfPermission(requireContext().applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            }
        } else {
            haveBluetoothAccess = true
        }

        binding.typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                when(i) { 0-> productViewModel.getOrderList()
                else-> productViewModel.getOrderListByType(when(i){1->2 2->1 3->3 else->0}) }
            } override fun onNothingSelected(p0: AdapterView<*>?) {/**/}
        }

        productViewModel.orderList.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                orderList.clear()
                orderList.addAll(it)
                binding.emptyLayout.isGONE()
                binding.recyclerview.isVISIBLE()
                binding.recyclerview.adapter = InvoiceListAdapter(orderList,this@InvoiceListFragment)
            } else {
                binding.recyclerview.isGONE()
                binding.emptyLayout.isVISIBLE()
            }
        }

        binding.searchBtn.setOnClickListener {
            if (binding.searchBar.visibility == View.GONE) {
                binding.spinnerLay.isGONE()
                binding.searchBar.isVISIBLE()
                binding.searchBar.requestFocus()
                Keyboard.showKeyboard(binding.searchBar)
                binding.searchBtn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_close_24)
            } else {
                Keyboard.hideSoftKeyBoard(requireContext(),binding.searchBar)
                binding.searchBar.isGONE()
                binding.spinnerLay.isVISIBLE()
                binding.searchBtn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_search_24)
                binding.searchBar.setText("")
            }
        }

        binding.searchBar.doAfterTextChanged {
            if (it != null && it.toString().isNotEmpty()) {
                val filterInvoice = filter(orderList, it.toString())
                binding.recyclerview.adapter = InvoiceListAdapter(filterInvoice,this@InvoiceListFragment)
            } else {
                binding.recyclerview.adapter = InvoiceListAdapter(orderList,this@InvoiceListFragment)
            }
        }

        /*productViewModel.loading.observe(viewLifecycleOwner) {
            if (it != null) {
                when { it-> ProgressLoader.show() else->  ProgressLoader.dismiss() }
            }
        }*/


        return binding.root
    }

    override fun onInvoiceListItemClick(orderItem: Order) {
        if (orderItem.order_id != null && orderItem.order_id.toString().isNotEmpty()) {
            productViewModel.getOrderDetailsById(orderItem.order_id)
            InvoiceViewDialog(requireContext(),orderItem,productViewModel,viewLifecycleOwner,haveBluetoothAccess,requireActivity()).show()
        } else {
            showErrorToast(requireContext(),"Something went wrong !")
        }
    }

    private fun filter(mainList: ArrayList<Order>, text: String): ArrayList<Order> {
        val filteredList = ArrayList<Order>()
        mainList.forEach {
            if (it.order_id.toString().contains(text) ||
                (it.outlet_name?:"").toString().lowercase().contains(text.lowercase()) ||
                (it.outlet_phone?:"").toString().contains(text) ||
                (it.order_date?:"").toString().contains(text)) {
                filteredList.add(it)
            }
        }
        return filteredList
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
                        haveBluetoothAccess = true
                    }
                } else {
                    haveBluetoothAccess = false
                }
                return
            }
        }
    }
}