package com.gdm.alphageeksales.view.ui.module.sales_and_order

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Brand
import com.gdm.alphageeksales.data.local.down_sync.Inventory
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.data.local.down_sync.PaymentType
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.databinding.DialogOrderScheduleBinding
import com.gdm.alphageeksales.databinding.FragmentOrderGenerationBinding
import com.gdm.alphageeksales.utils.*
import com.gdm.alphageeksales.view.adapter.SaleOrderAdapter
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import com.gdm.alphageeksales.viewmodels.OutletViewModel
import com.gdm.alphageeksales.viewmodels.ProductViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class OrderGenerationFragment : Fragment() {
    private lateinit var binding: FragmentOrderGenerationBinding
    private var salesOrderAdapter: SaleOrderAdapter? = null
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val outletViewModel: OutletViewModel by viewModels()
    private var salesOrderGenerationList = ArrayList<Brand>()
    private var inventoriesByBrand = ArrayList<Inventory>()
    private var paymentTypeList = ArrayList<PaymentType>()
    private var brandList = ArrayList<Brand>()
    private var outletData: Outlet? = null
    private var paymentType: Long? = 0
    private var totalAmount = 0.0
    private var saleType = 0
    private var date = ""
    private var time = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderGenerationBinding.inflate(layoutInflater)
        val currentTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        val calendar = Calendar.getInstance()
        Utils.startTime = currentTime.format(calendar.time)
        Utils.orderIdGlobal = System.currentTimeMillis()
        Utils.inventoryChangeList.clear()

        //sale_order start
        salesOrderGenerationList.add(Brand(-1, "Select Type"))
        salesOrderGenerationList.add(Brand(2, "Ready Stock Sale"))
        salesOrderGenerationList.add(Brand(1, "Order Generation"))
        binding.salesOrderSpinner.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item, salesOrderGenerationList)

        binding.salesOrderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?,view: View?, i: Int, l: Long) {
                if (i != 0) {
                    binding.brandLayout.isVISIBLE()
                    saleType = salesOrderGenerationList[i].id
                    productViewModel.getBrandList()
                } else {
                    binding.brandLayout.isGONE()
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        productViewModel.brandList.observe(viewLifecycleOwner) {
            if (it != null) {
                brandList.clear()
                brandList.addAll(it)
                brandList.add(0, Brand(-1, "Select Brand"))
                binding.brandSpinner.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, brandList)
            }
        }

        // get selected brand
        binding.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (i != 0) {
                    Utils.inventoryChangeList.clear()
                    binding.recyclerview.isVISIBLE()
                    binding.card.isVISIBLE()
                    binding.continueBtn.isVISIBLE()
                    productViewModel.getInventoryByBrand(brandList[i].id)
                    productViewModel.getPaymentTypes()
                } else {
                    productViewModel.getInventoryByBrand(0)
                    binding.recyclerview.isGONE()
                    binding.card.isGONE()
                    binding.continueBtn.isGONE()
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        productViewModel.inventoryByBrand.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                inventoriesByBrand.clear()
                inventoriesByBrand.addAll(it)
                val itemList = ArrayList<SalesOrderData>()
                it.forEach { item->
                    if ((item.product_name?:"").isNotEmpty() && item.product_id != null &&
                        item.category_id != null && item.brand_id != null && item.client_id != null) {

                        val orderGenItem = SalesOrderData(
                            brand_id = item.brand_id,
                            client_id = item.client_id,
                            product_name = item.product_name,
                            product_image = (item.product_image?:""),
                            product_id = item.product_id,
                            product_category_id = item.category_id,
                            unit_per_case = item.unit_per_case,
                            unit_price = item.unit_price,
                            case_qty = item.case_qty,
                            unit_qty = item.unit_qty,
                            cost_price = 0.0,
                            order_case_qty = 0,
                            order_unit_qty = 0
                        )
                        when(saleType) {
                            2-> if((orderGenItem.case_qty?:0)>0 || (orderGenItem.unit_qty?:0)>0){itemList.add(orderGenItem)}
                            else-> itemList.add(orderGenItem)
                        }
                    }
                }.also {
                    salesOrderAdapter = SaleOrderAdapter( itemList, object : ItemClickListener {
                        override fun onItemClick(id: Int) {
                            val salesOrderAdapterList = salesOrderAdapter?.getList()?:ArrayList(emptyList())
                            var givenAmount = 0.0
                            totalAmount = 0.0
                            if (salesOrderAdapterList.isNotEmpty()) {
                                for (i in salesOrderAdapterList) {
                                    totalAmount += i.cost_price?:0.0
                                }
                            } else {
                                totalAmount = 0.0
                            }
                            binding.totalSale.text = totalAmount.toString()
                            if (binding.paidAmount.text.isNotEmpty() && binding.paidAmount.text.toString() !=".") {
                                if (totalAmount < binding.paidAmount.text.toString().toDouble()) {
                                    givenAmount = binding.paidAmount.text.toString().toDouble() - totalAmount
                                    binding.changeAmount.text = givenAmount.toString()
                                    binding.dueAmount.text = "0.0"
                                } else {
                                    binding.changeAmount.text = "0.0"
                                    val dueAmount = totalAmount - binding.paidAmount.text.toString().toDouble()
                                    binding.dueAmount.text = dueAmount.toString()
                                }
                            } else {
                                binding.dueAmount.text = totalAmount.toString()
                                binding.changeAmount.text = "0.0"
                            }
                        }
                    }, saleType)
                    binding.recyclerview.adapter = salesOrderAdapter
                }
            }
        }

        productViewModel.paymentTypes.observe(viewLifecycleOwner) {
            paymentTypeList.clear()
            paymentTypeList.addAll(ArrayList(it))
            paymentTypeList.add(0,PaymentType(-1, "Select"))
            binding.spnrPaymentType.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, paymentTypeList)
        }

        binding.spnrPaymentType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItem = adapterView?.selectedItem?.toString()
                if (i != 0 && selectedItem != null) {
                    this@OrderGenerationFragment.paymentType = paymentTypeList[i].id
                } else {
                    paymentType = 0
                }
            } override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        binding.paidAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable) {
                var givenAmount = 0.0
                totalAmount = 0.0
                if (!salesOrderAdapter?.getList().isNullOrEmpty()) {
                    for (i in salesOrderAdapter?.getList()?:ArrayList()) {
                        totalAmount += i.cost_price?:0.0
                    }
                } else {
                    totalAmount = 0.0
                }
                binding.totalSale.text = totalAmount.toString()
                if (text.toString().isNotEmpty() && text.toString() !=".") {
                    if (totalAmount < text.toString().toDouble()) {
                        givenAmount = text.toString().toDouble() - totalAmount
                        binding.changeAmount.text = givenAmount.toString()
                        binding.dueAmount.text = "0.0"
                    } else {
                        binding.changeAmount.text = "0.0"
                        val dueAmount = totalAmount - text.toString().toDouble()
                        binding.dueAmount.text = dueAmount.toString()
                    }
                } else {
                    binding.dueAmount.text = totalAmount.toString()
                    binding.changeAmount.text = "0.0"
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int,count: Int, after: Int) {/**/}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {/**/}
        })

        outletViewModel.getOutletById(Utils.currentSchedule.outlet_id!!)
        outletViewModel.outlet.observe(viewLifecycleOwner) {
            if (it != null) {
                outletData = it
            }
        }

        binding.continueBtn.setOnClickListener {
            val salesOrderList = ArrayList<SalesOrderData>()
            var outOfStockCount = 0
            salesOrderAdapter?.getList()?.forEach { item->
                if ((item.order_case_qty?:0)>0 || (item.order_unit_qty?:0)>0) { salesOrderList.add(item) }
                if (saleType==2){ if((item.order_case_qty?:0)>(item.case_qty?:0)||(item.order_unit_qty?:0)>(item.unit_qty?:0)){outOfStockCount++}}
            }.also {
                when {
                    saleType == 0 -> {
                        showErrorToast(requireContext(),"Please select sale or order generator")
                    }
                    salesOrderList.isEmpty() -> {
                        showErrorToast(requireContext(),"No entry found !")
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
                    saleType == 2 && binding.dueAmount.text.toString().toDouble() != 0.0 -> {
                        showErrorToast(requireContext(),"Collect total sale amount")
                    }
                    else -> {
                        Utils.inventoryChangeList.apply { clear()
                            if (saleType == 2) {
                                salesOrderList.forEach { final->
                                    inventoriesByBrand.forEach { inventory ->
                                        if (inventory.product_id == final.product_id) {
                                            inventory.case_qty = (inventory.case_qty?:0)-(final.order_case_qty?:0)
                                            inventory.unit_qty = (inventory.unit_qty?:0)-(final.order_unit_qty?:0)
                                            add(inventory)
                                        }
                                    }
                                }
                            }
                        }.also {
                            val bundle = Bundle()
                            bundle.putString("order_list",Gson().toJson(salesOrderList))
                            bundle.putString("sale_type", saleType.toString())
                            bundle.putString("payment_type", paymentType.toString())
                            bundle.putString("payment_type_text",binding.spnrPaymentType.selectedItem.toString())
                            bundle.putString("total_sale", binding.totalSale.text.toString())
                            bundle.putString("paid_amount", binding.paidAmount.text.toString())
                            bundle.putString("due_amount", binding.dueAmount.text.toString())
                            bundle.putString("change_amount", binding.changeAmount.text.toString())
                            if (saleType == 2) {
                                findNavController().navigate(R.id.orderGenerationImageFragment, bundle)
                            } else if (saleType == 1) {
                                showOrderDialog(salesOrderList)
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun showOrderDialog(salesOrderAdapterList: ArrayList<SalesOrderData>) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBind = DialogOrderScheduleBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBind.root)

        //setup calender
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { date_picker, year, monthOfYear, dayOfMonth ->
            date_picker.minDate = System.currentTimeMillis() - 1000
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            date = sdf.format(calendar.time)
            dialogBind.dateTv.text = date
        }
        dialogBind.scheduleDateLayout.setOnClickListener {
            DatePickerDialog(
                requireActivity(),
                R.style.Calender_dialog_theme,
                dateSetListener,
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        dialogBind.scheduleTimeLayout.setOnClickListener {
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val mTimePicker = TimePickerDialog(
                requireActivity(), R.style.Calender_dialog_theme,
                { _, selectedHour, selectedMinute ->
                    val finalHour = when(selectedHour.toString()){"0","00"->"24" else-> selectedHour}
                    val finalMinute = when(selectedMinute.toString().length) {1-> "0${selectedMinute}" else-> selectedMinute.toString()}
                    time = "$finalHour:$finalMinute:00"
                    dialogBind.timeTv.text = time
                }, hour, minute, true
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }

        dialogBind.saveBtn.setOnClickListener {
            when {
                date.isEmpty() -> {
                    showErrorToast(requireContext(),"Please select date")
                }
                time.isEmpty() -> {
                    showErrorToast(requireContext(),"Please select time")
                }
                else -> {
                    dialog.dismiss()
                    val bundle = Bundle()
                    bundle.putString("order_list",Gson().toJson(salesOrderAdapterList))
                    bundle.putString("sale_type", saleType.toString())
                    bundle.putString("payment_type", paymentType.toString())
                    bundle.putString("total_sale", binding.totalSale.text.toString())
                    bundle.putString("paid_amount", binding.paidAmount.text.toString())
                    bundle.putString("due_amount", binding.dueAmount.text.toString())
                    bundle.putString("change_amount", binding.changeAmount.text.toString())
                    bundle.putString("date",date)
                    bundle.putString("time",time)
                    bundle.putBoolean("isDelivery", false)
                    findNavController().navigate(R.id.orderGenerationImageFragment, bundle)
                }
            }
        }

        // handle error
        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) {
            showErrorToast(requireContext(),it.toString())
        }

        //end schedule
        dialogBind.btnCancle.setOnClickListener { dialog.dismiss() }

        dialog.setCancelable(false)
        dialog.show()
        val width = resources.displayMetrics.widthPixels
        val window: Window? = dialog.window
        window?.setLayout((6 * width) / 7,WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}