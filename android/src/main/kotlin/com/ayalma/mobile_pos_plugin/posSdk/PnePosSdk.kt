package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity
import android.content.Intent
import android.device.PrinterManager
import android.graphics.BitmapFactory
import android.os.Bundle
import com.ayalma.mobile_pos_plugin.isPackageInstalled
import org.json.JSONException
import org.json.JSONObject

class PnePosSdk(private var activity: Activity,private var pcPosType: PcPosType) : PosSdk {
    var printerManager : PrinterManager = PrinterManager();
    private val PACKAGE_NAME = "ir.co.pna.pos"

    override fun print(byteArray: ByteArray, printEnd: (List<Any>) -> Unit) {
        printerManager.setupPage(381,-1)
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size);
        printerManager.drawBitmap(bmp,0,0);
        printerManager.printPage(0)
        printerManager.paperFeed(15)
        bmp.recycle()
        printEnd.invoke(ArrayList(0))
    }


    override fun getPrinterStatus(): PrinterStatus = when(printerManager.prn_getStatus()){
            0->PrinterStatus.Ok
            -1->PrinterStatus.NoPaper
            2->PrinterStatus.OverHeat
            -3->PrinterStatus.LowVol
            else->PrinterStatus.Unknown
    }

    var purchaseResultCallback :((PurchaseResultType, Map<String,Any?>) -> Unit)? = null
    var amount = "0"
    override fun purchase(amount: String, invoiceNumber: String, purchaseResultCallback: (PurchaseResultType,Map<String,Any?>) -> Unit) {
        this.purchaseResultCallback = purchaseResultCallback;
        if(!activity.isPackageInstalled(PACKAGE_NAME))
        {
            var resultJson = mapOf(Pair("reserveNumber",""), Pair("maskedPan",""),Pair("errorDescription","برنامه پوس نوین نصب نشده است"))
            purchaseResultCallback.invoke(PurchaseResultType.InitializationFailed , resultJson)
        }
        else {
            this.amount = amount
            val intent = Intent("ir.co.pna.pos.view.cart.IAPCActivity")
            intent.setPackage(PACKAGE_NAME)
            val bundle = Bundle()
            val jsonObject = JSONObject()
            try {
                jsonObject.put("AndroidPosMessageHeader", "@@PNA@@")
                jsonObject.put("ECRType", "1")
                jsonObject.put("Amount", amount)
                jsonObject.put("TransactionType", "00")

                //optional
                jsonObject.put("BillNo", "123456")
                jsonObject.put("AdditionalData", "123456")
                jsonObject.put("OriginalAmount", amount)
                jsonObject.put("SwipeCardTimeout", "30000")
                jsonObject.put("ReceiptType", "1") // customer and merchant:1 / customer only:2
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            bundle.putString("Data", jsonObject.toString())
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, 1002)
        }
    }

    override fun destroy() {}


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {

        if(requestCode == 1002) {
            if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                try {

                    val jsonObject = JSONObject(bundle.getString("Result"))

                    if (jsonObject.getString("Status").equals("OK", ignoreCase = true)) {
                        var resultJson = mapOf(
                                Pair("terminalNo",jsonObject.getString("TerminalID")),
                                Pair("traceNumber",jsonObject.getString("STAN")),
                                Pair("referenceNo",jsonObject.getString("RRN")),
                                Pair("amount",amount),
                                Pair("maskedPan",jsonObject.getString("CustomerCardNO")),
                                Pair("pcPosId",pcPosType.pcPosId),
                                Pair("creditTypeId",pcPosType.creditTypeId)
                        )
                        purchaseResultCallback?.invoke(PurchaseResultType.Succeed, resultJson)
                    } else {
                        var resultJson = mapOf(
                                Pair("errorCode",jsonObject.getString("ResponseCode")),
                                Pair("errorDescription",jsonObject.getString("Description"))
                        )

                        purchaseResultCallback?.invoke(PurchaseResultType.Failed,resultJson)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            else{
                purchaseResultCallback?.invoke(PurchaseResultType.Cancelled,emptyMap<String,Any?>())
            }
            return true
        }
        return false
    }
}