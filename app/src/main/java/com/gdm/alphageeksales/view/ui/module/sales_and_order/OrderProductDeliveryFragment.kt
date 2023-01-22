package com.gdm.alphageeksales.view.ui.module.sales_and_order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.databinding.FragmentOrderProductDeliveryBinding
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.showErrorToast
import com.gdm.alphageeksales.view.adapter.OrderDeliveryAdapter
import com.gdm.alphageeksales.viewmodels.ProductViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class OrderProductDeliveryFragment : Fragment() {
    private lateinit var binding: FragmentOrderProductDeliveryBinding
    private val productViewModel: ProductViewModel by viewModels()
    private var deliveryAdapter: OrderDeliveryAdapter? = null
    private var paymentTypeList: ArrayList<PaymentType> = ArrayList()
    private val deliveryList = ArrayList<SalesOrderData>()
    private var inventoriesByBrand = ArrayList<Inventory>()
    private var orderItem : Order? = null
    private var paymentType: Long? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderProductDeliveryBinding.inflate(layoutInflater)
        Utils.orderIdDelivery = System.currentTimeMillis()
        Utils.orderIdGlobal = System.currentTimeMillis()
        Utils.inventoryChangeList.clear()

        productViewModel.getPaymentTypes()
        productViewModel.paymentTypes.observe(viewLifecycleOwner) {
            productViewModel.getOrderItemById(Utils.currentSchedule.pre_order_id)
            paymentTypeList.clear()
            paymentTypeList.addAll(ArrayList(it))
            paymentTypeList.add(0,PaymentType(-1, "Select"))
            binding.paymentTypeSpn.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, paymentTypeList)
        }

        productViewModel.orderItem.observe(viewLifecycleOwner) {
            if (it != null) {
                productViewModel.getOrderDetailsById(it.order_id)
                orderItem = it
                binding.totalSale.text = it.grand_total.toString()

                when {
                    (it.due_amount?:0.0) > 0 -> { // have due amount
                        binding.totalSaleLay.isGONE()
                        binding.dueAmount.text = (orderItem?.due_amount?:0.0).toString()
                        binding.paidAmount.requestFocus()
                    }
                    else -> { // have no due amount
                        binding.paidAmount.setText(it.paid_amount.toString())
                        if((it.paid_amount?:0.0) > (it.grand_total?:0.0)) {
                            binding.changeAmount.text = ((it.paid_amount?:0.0)-(it.grand_total?:0.0)).toString()
                        }
                        try {
                            for (i in paymentTypeList.indices) {
                                if (paymentTypeList[i].id.toString() == it.payment_type) {
                                    binding.paymentTypeSpn.setSelection(i)
                                }
                            }
                        } catch (_: Exception) {}
                        binding.paidAmount.isEnabled = false
                        binding.paymentTypeSpn.isEnabled = false
                    }
                }
            }
        }

        // listen for product list according to brand
        productViewModel.orderDetailsById.observe(viewLifecycleOwner) { orderDetails->
            if (orderDetails != null && orderDetails.isNotEmpty()) {
                deliveryList.apply {
                    orderDetails.forEach { item->
                        add(SalesOrderData(
                            brand_id = item.brand_id,
                            client_id = item.client_id,
                            product_name = item.product_name,
                            product_image = item.product_image,
                            product_id = item.product_id,
                            product_category_id = item.category_id,
                            unit_per_case = item.unit_per_case,
                            unit_price = item.sales_price,
                            cost_price = item.price,
                            order_case_qty = item.order_case_qty,
                            order_unit_qty = item.order_unit_qty,
                            case_qty = 0,
                            unit_qty = 0
                        ))
                    }
                }.also {
                    productViewModel.getInventoryByBrand(it[0].brand_id)
                }
            }
        }

        productViewModel.inventoryByBrand.observe(viewLifecycleOwner) { inventories->
            inventories?.let { it.forEach { inventory ->
                deliveryList.forEach { delivery->
                    if (delivery.product_id == inventory.product_id) {
                        delivery.case_qty = (inventory.case_qty?:0)
                        delivery.unit_qty = (inventory.unit_qty?:0)
                    }
                } }
            }.also {
                binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                deliveryAdapter = OrderDeliveryAdapter(deliveryList)
                binding.recyclerview.adapter = deliveryAdapter
                inventoriesByBrand.clear()
                inventoriesByBrand.addAll(inventories)
            }
        }

        binding.paymentTypeSpn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                paymentType = if (i != 0 && selectedItem != null) { paymentTypeList[i].id } else { 0 }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        binding.paidAmount.doAfterTextChanged {
            if (it.toString().isNotEmpty() && it.toString() != ".") {
                if (it.toString().toDouble() <= (orderItem?.due_amount?:0.0)) {
                    binding.dueAmount.text = ((orderItem?.due_amount?:0.0)-it.toString().toDouble()).toString()
                    binding.changeAmount.text = "0.0"
                } else {
                    binding.changeAmount.text = (it.toString().toDouble()-(orderItem?.due_amount?:0.0)).toString()
                    binding.dueAmount.text = "0.0"
                }
            } else {
                binding.dueAmount.text = (orderItem?.due_amount?:0.0).toString()
                binding.changeAmount.text = "0.0"
            }
        }

        binding.continueBtn.setOnClickListener {
            val deliveryList = deliveryAdapter?.getList()
            var outOfStockCount = 0
            deliveryList?.forEach { item->
                if ((item.order_case_qty?:0)>(item.case_qty?:0)||(item.order_unit_qty?:0)>(item.unit_qty?:0)){outOfStockCount++}
            }.also {
                when {
                    deliveryList.isNullOrEmpty() -> {
                        showErrorToast(requireContext(),"No product found !")
                    }
                    outOfStockCount > 0 -> {
                        showErrorToast(requireContext(),"Insufficient product in stock")
                    }
                    paymentType == 0.toLong() -> {
                        showErrorToast(requireContext(),"Select payment type")
                    }
                    binding.paidAmount.text.isEmpty() -> {
                        binding.paidAmount.apply { requestFocus()
                            error = "Please give amount" }
                    }
                    binding.dueAmount.text.toString().isNotEmpty() && binding.dueAmount.text.toString().toDouble() > 0.0 -> {
                        showErrorToast(requireContext(),"Order Product Delivery is not possible with Due Amount")
                    }
                    else -> {
                        Utils.inventoryChangeList.apply { clear()
                            deliveryList.forEach { final->
                                inventoriesByBrand.forEach { inventory ->
                                    if (inventory.product_id == final.product_id) {
                                        inventory.case_qty = (inventory.case_qty?:0)-(final.order_case_qty?:0)
                                        inventory.unit_qty = (inventory.unit_qty?:0)-(final.order_unit_qty?:0)
                                        add(inventory)
                                    }
                                }
                            }
                        }.also {
                            val bundle = Bundle()
                            bundle.putString("order_list",Gson().toJson(deliveryList))
                            bundle.putString("sale_type", "3")
                            bundle.putString("payment_type", paymentType.toString())
                            bundle.putString("payment_type_text", binding.paymentTypeSpn.selectedItem.toString())
                            bundle.putString("total_sale", binding.totalSale.text.toString())
                            bundle.putString("paid_amount", binding.paidAmount.text.toString())
                            bundle.putString("change_amount", binding.changeAmount.text.toString())
                            bundle.putString("due_amount", binding.dueAmount.text.toString())
                            findNavController().navigate(R.id.orderGenerationImageFragment, bundle)
                        }
                    }
                }
            }
        }


        return binding.root
    }
}