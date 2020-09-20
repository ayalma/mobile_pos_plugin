package com.ayalma.mobile_pos_plugin.posSdk.Parsian

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ayalma.mobile_pos_plugin.isPackageInstalled
import com.ayalma.mobile_pos_plugin.posSdk.PcPosType
import com.ayalma.mobile_pos_plugin.posSdk.PosSdk
import com.ayalma.mobile_pos_plugin.posSdk.PrinterStatus
import com.ayalma.mobile_pos_plugin.posSdk.PurchaseResultType

class ParsianPosSdk(private var activity: Activity, private var pcPosType: PcPosType) : PosSdk, BroadcastReceiver() {
    private val PACKAGE_NAME = "ir.co.pna.pos"
    override fun print(byteArray: ByteArray, printEnd: (List<Any>) -> Unit) {

    }

    override fun getPrinterStatus(): PrinterStatus {
    return  PrinterStatus.LowTemp;
    }

    var purchaseResultCallback :((PurchaseResultType, Map<String,Any?>) -> Unit)? = null
    override fun purchase(amount: String, invoiceNumber: String, purchaseResultCallback: (PurchaseResultType, Map<String, Any?>) -> Unit) {
        this.purchaseResultCallback = purchaseResultCallback;
        if(!activity.isPackageInstalled(PACKAGE_NAME))
        {
            var resultJson = hashMapOf<String,Any?>(
                    "status" to 0,
                    "statusDescription" to "",
                    "errorDescription" to "پارسیان نصب نشده است"
            )
            purchaseResultCallback.invoke(PurchaseResultType.InitializationFailed , resultJson)
        }
        else{

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =false
    override fun onReceive(context: Context, intent: Intent) {

    }
}

