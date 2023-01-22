package com.gdm.alphageeksales.view.ui.module.sales_and_order

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.PopupMenu
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.databinding.DialogOrderGenerationShareBinding
import com.gdm.alphageeksales.utils.ItemClickListener
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.view.adapter.*
import com.gkemon.XMLtoPDF.PdfGenerator
import com.gkemon.XMLtoPDF.PdfGeneratorListener
import com.gkemon.XMLtoPDF.model.FailureResponse
import com.gkemon.XMLtoPDF.model.SuccessResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OrderGenerationShareDialog(context: Context,
                                 private val module: String?,
                                 private val moduleInfo: String?,
                                 private val note: String?,
                                 private val salesOrderList: String?,
                                 private val imageUri: Uri?,
                                 private val totalSale: String?,
                                 private val amountPaid: String?,
                                 private val dueAmount: String?,
                                 private val changeAmount: String?,
                                 private val paymentType: String?) : Dialog(context) {

    private lateinit var binding: DialogOrderGenerationShareBinding
    private var shareMenu: PopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogOrderGenerationShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        val width = context.resources.displayMetrics.widthPixels
        window?.setLayout((8 * width) / 9, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.module.text = module ?: ""
        binding.moduleInfo.text = moduleInfo ?: ""

        shareMenu = PopupMenu(context,binding.shareBtn)
        shareMenu?.menuInflater?.inflate(R.menu.share_menu,shareMenu?.menu)
        shareMenu?.setForceShowIcon(true)
        shareMenu?.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem: MenuItem? ->
            when(menuItem?.itemId) {
                R.id.share -> {
                    PdfGenerator.getBuilder()
                        .setContext(context)
                        .fromViewSource()
                        .fromView(binding.content)
                        .setFileName(module)
                        .setFolderNameOrPath("com.alphageeksales.share")
                        .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.SHARE)
                        .build( object : PdfGeneratorListener() {
                            override fun onFailure(failureResponse: FailureResponse) { super.onFailure(failureResponse) }
                            override fun showLog(log: String) { super.showLog(log) }
                            override fun onStartPDFGeneration() {/**/}
                            override fun onFinishPDFGeneration() {/**/}
                            override fun onSuccess(response: SuccessResponse) { super.onSuccess(response) }
                        })
                }
                R.id.save -> {
                    PdfGenerator.getBuilder()
                        .setContext(context)
                        .fromViewSource()
                        .fromView(binding.content)
                        .setFileName(module)
                        .setFolderNameOrPath("com.alphageeksales.share")
                        .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.OPEN)
                        .build( object : PdfGeneratorListener() {
                            override fun onFailure(failureResponse: FailureResponse) { super.onFailure(failureResponse) }
                            override fun showLog(log: String) { super.showLog(log) }
                            override fun onStartPDFGeneration() {/**/}
                            override fun onFinishPDFGeneration() {/**/}
                            override fun onSuccess(response: SuccessResponse) { super.onSuccess(response) }
                        })
                }
            }
            return@OnMenuItemClickListener true
        })

        binding.shareBtn.setOnClickListener { shareMenu?.show() }

        if (salesOrderList != null && salesOrderList.isNotEmpty()) {
            val productsList = Gson().fromJson<ArrayList<SalesOrderData>?>(salesOrderList,object : TypeToken<ArrayList<SalesOrderData>?>(){}.type)
            if (productsList != null && productsList.isNotEmpty()) {
                binding.productLayout.isVISIBLE()
                binding.productRecycler.adapter = SaleOrderAdapter(productsList,object :
                    ItemClickListener { override fun onItemClick(id: Int) {} },null,true)
            }
            binding.totalSaleAmount.text = totalSale?:"0"
            binding.paidAmount.text = amountPaid?:"0"
            binding.dueAmount.text = dueAmount?:"0"
            binding.changeAmount.text = changeAmount?:"0"
            binding.paymentType.text = paymentType?:""
        }

        if (imageUri != null){
            binding.imageLayout.isVISIBLE()
            binding.image.setImageURI(imageUri)
        }

        if (!note.isNullOrEmpty()) {
            binding.noteLayout.isVISIBLE()
            binding.note.text = note
        }
    }
}