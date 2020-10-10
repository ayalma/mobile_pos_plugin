package com.ayalma.mobile_pos_plugin.posSdk

import io.flutter.plugin.common.PluginRegistry

interface PosSdk: PluginRegistry.ActivityResultListener {
    /***
     * print
     */
    fun  print(byteArray:ByteArray,printEnd:(List<Any>)->Unit)
    fun  getPrinterStatus():PrinterStatus
    fun  purchase(amount:String,invoiceNumber:String,purchaseResultCallback:(PurchaseResultType,Map<String,Any?>)->Unit)
    fun destroy();
}