package com.gdm.alphageeksales.view.ui.module.invoice

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.MenuItem
import android.webkit.WebView
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.dantsu.escposprinter.exceptions.EscPosEncodingException
import com.dantsu.escposprinter.exceptions.EscPosParserException
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Order
import com.gdm.alphageeksales.data.local.down_sync.OrderDetails
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.databinding.InvoiceViewDialogBinding
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.viewmodels.ProductViewModel
import com.gkemon.XMLtoPDF.PdfGenerator
import com.gkemon.XMLtoPDF.PdfGeneratorListener
import com.gkemon.XMLtoPDF.model.FailureResponse
import com.gkemon.XMLtoPDF.model.SuccessResponse
import com.google.gson.Gson

class InvoiceViewDialog(context: Context,
                        private val orderItem: Order?,
                        private val productViewModel: ProductViewModel,
                        private val lifecycleOwner: LifecycleOwner,
                        private val haveBluetoothAccess: Boolean,
                        private val activity: Activity) : Dialog(context) {

    private lateinit var binding: InvoiceViewDialogBinding
    private var printMenu: PopupMenu? = null
    private var shareMenu: PopupMenu? = null
    private val orderDetails = arrayListOf<OrderDetails>()
    private val printJobs = arrayListOf<PrintJob>()
    private var invoiceWebView: WebView? = null
    private var wifi = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InvoiceViewDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        window?.setLayout((8 * width) / 9,(9 * height) / 10)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SharedPref.init(context)

        productViewModel.orderDetailsById.observe(lifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                orderDetails.clear()
                orderDetails.addAll(it)
                productViewModel.getAppSettings()
            }
        }

        binding.closeBtn.setOnClickListener { dismiss() }

        printMenu = PopupMenu(context,binding.shareBtn)
        printMenu?.menuInflater?.inflate(R.menu.print_menu,printMenu?.menu)
        printMenu?.setForceShowIcon(true)
        printMenu?.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem: MenuItem? ->
            when(menuItem?.itemId) {
                R.id.wifi -> if (invoiceWebView != null) { createWebViewPrintJob(invoiceWebView!!) }
                R.id.bluetooth -> if (haveBluetoothAccess) {
                    ActivityCompat.requestPermissions(activity,arrayOf(Manifest.permission.BLUETOOTH_CONNECT),1)
                } else {
                    wifi = false
                    productViewModel.getAppSettings()
                }
            }
            return@OnMenuItemClickListener true
        })

        binding.printBtn.setOnClickListener { printMenu?.show() }

        shareMenu = PopupMenu(context,binding.shareBtn)
        shareMenu?.menuInflater?.inflate(R.menu.share_menu,shareMenu?.menu)
        shareMenu?.setForceShowIcon(true)
        shareMenu?.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem: MenuItem? ->
            when(menuItem?.itemId) {
                R.id.share -> {
                    PdfGenerator.getBuilder()
                        .setContext(context)
                        .fromViewSource()
                        .fromView(binding.invoiceWebView)
                        .setFileName("Invoice(${orderItem?.order_id?:0})")
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
                        .fromView(binding.invoiceWebView)
                        .setFileName("Invoice(${orderItem?.order_id?:0})")
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

        productViewModel.appSettings.observe(lifecycleOwner) {
            if (it != null) {
                if (wifi) {
                    invoiceWebView = WebView(context)
                    var serial = 0
                    // Generate an HTML document on the fly:
                    var htmlDocument = """<html><style> p.small { font-variant: small-caps;  font-size: 12px;text-align:center;" }
                table, th, td { border-collapse: collapse; }
                th, td { text-align: left; font-size: 9px }
                div.a { font-size: 11px; }
                div.b {font-size: large;}
                hr.new2 { border-top: 1px dashed black; }hr.new1 { border-top: .2px solid black; } p.small { line-height: 0.7; } 
                div.c { font-size: 150%; }.center {text-align: center; border: 3px solid green; }
                div { width: 270px; margin: auto;  padding: 1px; font-size: 10px;  background-color: white; text-align:center; 
                </style> <div><body style= "margin:0px; text-align: center;"> <img style = "height: 88px; width: 121px; 
                 margin-bottom:0px" src="file:///android_res/drawable/splash_logo.png">
                <p style=font-size:14px; margin-bottom:0px; > ${it.time_zone}</p> 
                <p style=font-size:14px; margin-bottom:0px; >Email : ${it.email}</p>
                <p style=font-size:14px; margin-bottom:0px; > Date : ${orderItem!!.order_date} <br> 
                <p style=font-size:14px > Outlet Name : ${orderItem.outlet_name}</p>
                <p style=font-size:14px > Outlet Phone : ${orderItem.outlet_phone}</p>
                <p style=font-size:14px > Invoice Type : ${when(orderItem.order_type.toString()){ "1"->{"Order Generation"} "2"->{"Ready Stock Sales"} "3"->{"Order Product Delivery"} else->""}}</p>
                <table style="width:100%; padding-left:10px; padding-right:10px;"> <tr>
                <td style="text-align:left; font-size: 16px; font-weight:bold">SL</td>
                <td style="text-align:left; font-size: 16px; font-weight:bold">${context.getString(R.string.product_name)}</td>
                <td style="text-align:left;font-size: 16px; font-weight:bold">${context.getString(R.string.unit)}</td>
                <td style="text-align:left;font-size: 16px; font-weight:bold">${context.getString(R.string.case_)}</td> 
                <td style="text-align:right;font-size: 16px; font-weight:bold">${context.getString(R.string.amount)}</td>
                </tr>
                """
                    var htmlInvoice = ""
                    for (i in orderDetails) {
                        serial += 1
                        htmlInvoice = htmlInvoice + "" +
                                "<tr><td " +
                                "style=font-size:14px>${serial}</td>" +
                                "<td style=font-size:14px>${i.product_name}</td>" +
                                "<td style=text-align:center;font-size:14px;> ${i.order_unit_qty}</td>" +
                                "<td style=text-align:center;font-size:14px; > ${i.order_case_qty}</td>" +
                                "<td style=text-align:right;font-size:14px; >${i.price}</td></tr> "
                    }
                    htmlInvoice + "</table>  \n" + "<hr class=\"new2\">"
                    htmlDocument += htmlInvoice
                    val grandTotal = "<table  style=\"width:100%; padding-left:10px; padding-top:0px; padding-bottom:0px; padding-right:10px; border: .5px solid white;\"><tr> <td style=\"text-align:left;font-size: 15px; font-weight:bold\"> " + "Grand Total" + "" + "</td>  <td  style=\"text-align:right;font-size: 15px; font-weight:bold\"> ${orderItem.grand_total}</td></tr></table>"
                    val paidAmount = "<table  style=\"width:100%; padding:10px; padding-top:0px; padding-bottom:0px; padding-right:10px; border: .5px solid white;\"><tr> <td style=\"text-align:left;font-size: 13px;\"> " + "Paid Amount" + "</td>  <td  style=\"text-align:right;font-size: 13px \"> ${orderItem.paid_amount}</td></tr></table>"
                    val dueAmount = "<table  style=\"width:100%; padding:10px; padding-top:0px; padding-bottom:0px; padding-right:10px; border: .5px solid white;\"><tr> <td style=\"text-align:left;font-size: 13px\">" + "Due Amount" + "</td>  <td  style=\"text-align:right;font-size: 13px \">${orderItem.due_amount}</td> </tr></table>"
                    val changeAmount = "<table  style=\"width:100%; padding:10px; padding-top:0px; padding-bottom:0px; padding-right:10px; border: .5px solid white;\"><tr> <td style=\"text-align:left;font-size: 13px\">" + "Change Amount" + "</td>  <td  style=\"text-align:right;font-size: 13px \">${getChangeAmount(orderItem.paid_amount,orderItem.grand_total)}</td></tr></table>"
                    val receiptNo = "<table  style=\"width:100%; border: .5px solid white;\"><tr> <td style=\"text-align:center;font-size: 13px;\"> Receipt No : ${orderItem.order_id}</td> </tr></table> "
                    val userName = "<table  style=\"width:100%; border: .5px solid white;\"><tr> <td style=\"text-align:center;font-size: 13px;\"> Sales Person : ${orderItem.user_name}</td></tr></table>"
                    val userPhone = "<table  style=\"width:100%; border: .5px solid white;\"><tr> <td style=\"text-align:center;font-size: 13px;\"> Phone : ${SharedPref.read("PHONE","")}</td></tr></table> "
                    val lastPart = ("$grandTotal<hr class=\"new2\"><p style=\"text-align:left;font-size: 13px;\">$paidAmount$dueAmount$changeAmount<hr class=\"new2\"></div></body></html>")
                    htmlDocument = htmlDocument + lastPart + receiptNo + userName + userPhone
                    invoiceWebView?.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
                    binding.invoiceWebView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
                } else {
                    wifi = true
                    var position = 1
                    var orderDetailStr = ""
                    for (items in orderDetails) {
                        orderDetailStr = orderDetailStr + "[L]" + position + "- " + items.product_name + "- " + items.order_unit_qty + " - " + items.order_case_qty + " - [R]" + items.price + "\n"
                        position += 1
                    }
                    var items = ""
                    for (i in orderDetails.indices) {
                        items = "$items[L]<b>${orderDetails[i].product_name}</b>[L]${orderDetails[i].order_unit_qty} x${orderDetails[i].order_unit_qty}[R]<b>${(orderDetails[i].price)}</b>"
                    }
                    try {
                        val connection = BluetoothPrintersConnections.selectFirstPaired()
                        val printer = EscPosPrinter(connection, 203, 48f, 32)
                        printer.printFormattedTextAndCut("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,ContextCompat.getDrawable(context,R.drawable.print_logo))
                                    + "</img>\n\n"
                                    + "[C]<b>" + it.time_zone + "</b>\n"
                                    + "[C]Email : " + it.email + "\n"
                                    + "[C]Date :" + orderItem?.order_date?.trim() + "\n"
                                    + "[C]Outlet Name : " + orderItem?.outlet_name + "\n"
                                    + "[C]Outlet Phone : " + orderItem?.outlet_phone + "\n"
                                    + "[C]Invoice Type : ${when(orderItem?.order_type.toString()){ "1"->{"Order Generation"} "2"->{"Ready Stock Sales"} "3"->{"Order Product Delivery"} else->"" }}\n"
                                    + "[L]\n" +
                                    "[L]<b><font size='small'>SL </font></b>" + "<b>Product Name </b><b>Unit </b><b>Case </b><b>[R]Price</b>\n" +
                                    orderDetailStr
                                    + "-----------------------------\n"
                                    + "[L]<b>Grand Total : [R]" + orderItem?.grand_total + "</b> \n"
                                    + "[L]<b>Paid Amount : [R]" + orderItem?.paid_amount.toString() + "</b> \n"
                                    + "[L]<b>Due Amount : [R]" + orderItem?.due_amount + "</b> \n"
                                    + "[L]<b>Change Amount : [R]" + getChangeAmount(orderItem?.paid_amount, orderItem?.grand_total).toString() + "</b> \n"
                                    + "-----------------------------\n"
                                    + "[C]Receipt No : " + orderItem?.order_id + "\n"
                                    + "[C]Sales Person : " + orderItem?.user_name + "\n"
                                    + "[C]Phone : " + SharedPref.read("PHONE","") + "\n"
                        )
                    } catch (e: EscPosConnectionException) {
                        e.printStackTrace()
                    } catch (e: EscPosParserException) {
                        e.printStackTrace()
                    } catch (e: EscPosEncodingException) {
                        e.printStackTrace()
                    } catch (e: EscPosBarcodeException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun createWebViewPrintJob(webView: WebView) {
        val margins = PrintAttributes.Margins(1, 1, 1, 1)
        val attributes = PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.JPN_CHOU4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR).setMinMargins(margins).build()
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = context.getString(R.string.app_name) + " Document"
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        val printJob: PrintJob = printManager.print(jobName, printAdapter, attributes)
        printJobs.add(printJob)
    }

    private fun getChangeAmount(paidAmount: Double?, grandTotal: Double?): Double? {
        return if ((paidAmount ?: 0.0) > (grandTotal ?: 0.0)) {
            (paidAmount ?: 0.0) - (grandTotal ?: 0.0)
        } else {
            0.0
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        invoiceWebView = null
        printJobs.clear()
        wifi = true
    }
}