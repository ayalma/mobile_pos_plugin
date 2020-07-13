package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity
import android.content.Intent
import android.device.PrinterManager
import android.graphics.BitmapFactory
import android.os.Bundle
import com.ayalma.mobile_pos_plugin.isPackageInstalled
import org.json.JSONException
import org.json.JSONObject

class PnePosSdk(private var activity: Activity) : PosSdk {
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

    var purchaseResultCallback :((PurchaseResultType, List<Any?>) -> Unit)? = null
    override fun purchase(amount: String, invoiceNumber: String, purchaseResultCallback: (PurchaseResultType, List<Any?>) -> Unit) {
        this.purchaseResultCallback = purchaseResultCallback;
        if(!activity.isPackageInstalled(PACKAGE_NAME))
        {
            purchaseResultCallback.invoke(PurchaseResultType.Failed , listOf("","","برنامه پوس نوین نصب نشده است"))
        }
        else {

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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {

        if(requestCode == 1002) {
            if (resultCode == Activity.RESULT_OK) {
                val bundle = data!!.extras
                try {

                    val jsonObject = JSONObject(bundle.getString("Result"))

                    if (jsonObject.getString("Status").equals("OK", ignoreCase = true)) {
//                        result += """
//                        شماره ترمینال:  ${jsonObject.getString("TerminalID")}
//
//                        """.trimIndent()
//                        if (jsonObject.has("STAN")) {
//                            result += """
//                            شماره پیگیری:  ${jsonObject.getString("STAN")}
//
//                            """.trimIndent()
//                        }
//                        if (jsonObject.has("RRN")) {
//                            result += """
//                            شماره مرجع:  ${jsonObject.getString("RRN")}
//
//                            """.trimIndent()
//                        }
//                        result += """
//                        کد پاسخ:  ${jsonObject.getString("ResponseCode")}
//
//                        """.trimIndent()
//                        result += """
//                        شماره کارت: ${jsonObject.getString("CustomerCardNO")}
//
//                        """.trimIndent()
//                        result += """
//                        زمان تراکنش:  ${jsonObject.getString("TransactionDateTime")}
//
//                        """.trimIndent()
//                        //                    result +=  "وضعیت تراکنش" + ":  " + ((jsonObject.getString("Status").equalsIgnoreCase("OK"))?"موفق":"ناموفق") + "\n";
////                    result +=  "شماره پذیرنده" + ":  " + jsonObject.getString("MerchantId") + "\n";
////                    result +=  "کد پستی" + ":  " + jsonObject.getString("PostalCode") + "\n";
                        purchaseResultCallback?.invoke(PurchaseResultType.Succeed, arrayListOf(jsonObject.getString("TerminalID"), jsonObject.getString("MerchantId"), "posSerial", "reserveNumber", "traceNumber", jsonObject.getString("RRN"), "ref", "amount", "txnDate", "txnTime", "maskedPan"))

                    } else {

//                        if (jsonObject.has("ResponseCode")) {
//                            result += """
//                            کد پاسخ:  ${jsonObject.getString("ResponseCode")}
//
//                            """.trimIndent()
//                        }
//
//
//                        if (jsonObject.has("CustomerCardNO")) {
//                            result += """
//                            شماره کارت: ${}
//
//                            """.trimIndent()
//                        }
//                        if (jsonObject.has("TransactionDateTime")) {
//                            result += """
//                            زمان تراکنش:  ${jsonObject.getString("TransactionDateTime")}
//
//                            """.trimIndent()
//                        }

                        purchaseResultCallback?.invoke(PurchaseResultType.Failed, arrayListOf(jsonObject.getString("ResponseCode"), jsonObject.getString("Description"), jsonObject.getString("TerminalID"), jsonObject.getString("MerchantId"), "posSerial", "reserveNumber", "traceNumber", "rrn", "ref", "amount", "txnDate", "txnTime", "maskedPan"))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return true;
        }
        return false;
    }
}