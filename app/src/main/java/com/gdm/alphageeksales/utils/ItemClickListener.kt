package com.gdm.alphageeksales.utils

import com.gdm.alphageeksales.data.local.down_sync.Order

interface ItemClickListener {
    fun onItemClick(id :Int)
}
interface InvoiceListItemClickListener{
    fun onInvoiceListItemClick(orderItem: Order)
}