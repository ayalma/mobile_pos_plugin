package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity
import android.content.Intent
import com.ayalma.mobile_pos_plugin.MobilePosPlugin
import com.ayalma.mobile_pos_plugin.print.FactorPrintableData
import com.kishcore.sdk.hybrid.api.DataCallback
import com.kishcore.sdk.hybrid.api.HostApp
import com.kishcore.sdk.hybrid.api.SDKManager
import com.kishcore.sdk.sep.rahyab.api.PaymentCallback
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RahyabPosSdk(private var activity: Activity) : PosSdk {
    var hostApp:HostApp = SDKManager.init(activity)

    override fun print(byteArray: ByteArray, printEnd: (List<Any>) -> Unit) {
        SDKManager.print(activity, FactorPrintableData(byteArray), DataCallback{ data-> printEnd.invoke(data.toList())})
    }

    override fun getPrinterStatus(): PrinterStatus =
        when(SDKManager.getPrinterStatus()){
            0->PrinterStatus.NoPaper
            1->PrinterStatus.Ok
            -1->PrinterStatus.Unknown
            3->PrinterStatus.OverHeat
            4->PrinterStatus.LowVol
            5->PrinterStatus.PaperJam
            6->PrinterStatus.Busy
            7->PrinterStatus.LiftHead
            8->PrinterStatus.CutPositionError
            9->PrinterStatus.LowTemp
            else -> PrinterStatus.Unknown
        }


    override fun purchase(amount: String, invoiceNumber: String, purchaseResultCallback: (PurchaseResultType, Map<String,Any?>) -> Unit) {
        when(hostApp){
            HostApp.FANAVA-> fanavaPurchase(activity,"",amount,purchaseResultCallback)
            HostApp.SEP -> sepPurchase(activity,"",amount,purchaseResultCallback)
            HostApp.PEC -> pecPurchase(activity,"",amount,purchaseResultCallback)
        }

    }

    private fun fanavaPurchase(activity: Activity, invoiceNumber:String, amount:String, purchaseResultCallback: (PurchaseResultType, Map<String,Any?>) -> Unit){
        com.kishcore.sdk.fanava.rahyab.api.SDKManager.purchase(activity, invoiceNumber, amount, object : com.kishcore.sdk.fanava.rahyab.api.PaymentCallback {
            override fun onPaymentInitializationFailed(reserveNumber: String?, maskedPan: String?, errorDescription: String?,panHash:String?) {
               var resultJson = hashMapOf<String,Any?>(
                       "reserveNumber" to reserveNumber,
                       "maskedPan" to maskedPan,
                       "errorDescription" to errorDescription,
                       "panHash" to panHash)
                purchaseResultCallback.invoke(PurchaseResultType.InitializationFailed,resultJson)

            }

            override fun onPaymentCancelled(reserveNumber: String?, maskedPan: String?,panHash:String?) {
                var resultJson = mapOf(Pair("reserveNumber",reserveNumber), Pair("maskedPan",maskedPan), Pair("panHash",panHash))
                purchaseResultCallback.invoke(PurchaseResultType.Cancelled,resultJson)
            }

            override fun onPaymentSucceed(terminalNo: String?, merchantId: String?, posSerial: String?, reserveNumber: String?, traceNumber: String?, rrn: String?, ref: String?, amount: String?, txnDate: String?, txnTime: String?, maskedPan: String?,panHash:String?) {
                var resultJson = mapOf(
                        Pair("terminalNo",terminalNo),
                        Pair("merchantId",merchantId),
                        Pair("posSerial",posSerial),
                        Pair("reserveNumber",reserveNumber),
                        Pair("traceNumber",traceNumber),
                        Pair("rrn",rrn),
                        Pair("ref",ref),
                        Pair("amount",amount),
                        Pair("txnDate",txnDate),
                        Pair("txnTime",txnTime),
                        Pair("maskedPan",maskedPan),
                        Pair("panHash",panHash)
                        )
                purchaseResultCallback.invoke(PurchaseResultType.Succeed,resultJson);

            }

            override fun onPaymentFailed(errorCode: Int, errorDescription: String, terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String,panHash:String?) {
                var resultJson = mapOf(
                        Pair("errorCode",errorCode),
                        Pair("errorDescription",errorDescription),
                        Pair("terminalNo",terminalNo),
                        Pair("merchantId",merchantId),
                        Pair("posSerial",posSerial),
                        Pair("reserveNumber",reserveNumber),
                        Pair("traceNumber",traceNumber),
                        Pair("rrn",rrn),
                        Pair("ref",ref),
                        Pair("amount",amount),
                        Pair("txnDate",txnDate),
                        Pair("txnTime",txnTime),
                        Pair("maskedPan",maskedPan),
                        Pair("panHash",panHash)
                )
                purchaseResultCallback.invoke(PurchaseResultType.Failed,resultJson)

            }
        })
    }

    private  fun sepPurchase(activity: Activity,invoiceNumber: String,amount: String,purchaseResultCallback: (PurchaseResultType, Map<String,Any?>) -> Unit){
        com.kishcore.sdk.sep.rahyab.api.SDKManager.purchase(activity, invoiceNumber, amount, object : PaymentCallback {
            override fun onPaymentInitializationFailed(reserveNumber: String?, maskedPan: String?, errorDescription: String?) {
                var resultJson = hashMapOf<String,Any?>(
                        "reserveNumber" to reserveNumber,
                        "maskedPan" to maskedPan,
                        "errorDescription" to errorDescription)
                purchaseResultCallback.invoke(PurchaseResultType.InitializationFailed,resultJson)
            }

            override fun onPaymentCancelled(reserveNumber: String?, maskedPan: String?) {
                var resultJson = mapOf(Pair("reserveNumber",reserveNumber), Pair("maskedPan",maskedPan))
                purchaseResultCallback.invoke(PurchaseResultType.Cancelled,resultJson)
            }

            override fun onPaymentSucceed(terminalNo: String?, merchantId: String?, posSerial: String?, reserveNumber: String?, traceNumber: String?, rrn: String?, ref: String?, amount: String?, txnDate: String?, txnTime: String?, maskedPan: String?) {
                var resultJson = mapOf(
                        Pair("terminalNo",terminalNo),
                        Pair("merchantId",merchantId),
                        Pair("posSerial",posSerial),
                        Pair("reserveNumber",reserveNumber),
                        Pair("traceNumber",traceNumber),
                        Pair("rrn",rrn),
                        Pair("ref",ref),
                        Pair("amount",amount),
                        Pair("txnDate",txnDate),
                        Pair("txnTime",txnTime),
                        Pair("maskedPan",maskedPan)

                )
                purchaseResultCallback.invoke(PurchaseResultType.Succeed,resultJson)
            }


            override fun onPaymentFailed(errorCode: Int, errorDescription: String, terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String) {
                var resultJson = mapOf(
                        Pair("errorCode",errorCode),
                        Pair("errorDescription",errorDescription),
                        Pair("terminalNo",terminalNo),
                        Pair("merchantId",merchantId),
                        Pair("posSerial",posSerial),
                        Pair("reserveNumber",reserveNumber),
                        Pair("traceNumber",traceNumber),
                        Pair("rrn",rrn),
                        Pair("ref",ref),
                        Pair("amount",amount),
                        Pair("txnDate",txnDate),
                        Pair("txnTime",txnTime),
                        Pair("maskedPan",maskedPan)
                )
                purchaseResultCallback.invoke(PurchaseResultType.Failed,resultJson)
            }
        })
    }
    private fun pecPurchase(activity: Activity,invoiceNumber: String,amount: String,purchaseResultCallback: (PurchaseResultType, Map<String,Any?>) -> Unit)
    {
        com.kishcore.sdk.parsian.rahyab.api.SDKManager.purchase(activity, invoiceNumber, amount, object : com.kishcore.sdk.parsian.rahyab.api.PaymentCallback {
            override fun onPaymentInitializationFailed(reserveNumber: String, maskedPan: String, errorDescription: String) {
                var resultJson = hashMapOf<String,Any?>(
                        "reserveNumber" to reserveNumber,
                        "maskedPan" to maskedPan,
                        "errorDescription" to errorDescription)
                purchaseResultCallback.invoke(PurchaseResultType.InitializationFailed,resultJson)
            }

            override fun onPaymentSucceed(terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String) {
                var resultJson = mapOf(
                        Pair("terminalNo",terminalNo),
                        Pair("merchantId",merchantId),
                        Pair("posSerial",posSerial),
                        Pair("reserveNumber",reserveNumber),
                        Pair("traceNumber",traceNumber),
                        Pair("rrn",rrn),
                        Pair("ref",ref),
                        Pair("amount",amount),
                        Pair("txnDate",txnDate),
                        Pair("txnTime",txnTime),
                        Pair("maskedPan",maskedPan)

                )
                purchaseResultCallback.invoke(PurchaseResultType.Succeed,resultJson)
            }

            override fun onPaymentFailed(errorCode: Int, errorDescription: String, terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String) {
                var resultJson = mapOf(
                        Pair("errorCode",errorCode),
                        Pair("errorDescription",errorDescription),
                        Pair("terminalNo",terminalNo),
                        Pair("merchantId",merchantId),
                        Pair("posSerial",posSerial),
                        Pair("reserveNumber",reserveNumber),
                        Pair("traceNumber",traceNumber),
                        Pair("rrn",rrn),
                        Pair("ref",ref),
                        Pair("amount",amount),
                        Pair("txnDate",txnDate),
                        Pair("txnTime",txnTime),
                        Pair("maskedPan",maskedPan)
                )
                purchaseResultCallback.invoke(PurchaseResultType.Failed,resultJson)
            }
            override fun onPaymentCancelled(reserveNumber: String, maskedPan: String) {
                var resultJson = mapOf(Pair("reserveNumber",reserveNumber), Pair("maskedPan",maskedPan))
                purchaseResultCallback.invoke(PurchaseResultType.Cancelled,resultJson)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean=false


}