package com.ayalma.mobile_pos_plugin.posSdk.Parsian

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.util.Log
import com.ayalma.mobile_pos_plugin.isPackageInstalled
import com.ayalma.mobile_pos_plugin.posSdk.PcPosType
import com.ayalma.mobile_pos_plugin.posSdk.PosSdk
import com.ayalma.mobile_pos_plugin.posSdk.PrinterStatus
import com.ayalma.mobile_pos_plugin.posSdk.PurchaseResultType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ParsianPosSdk(private var activity: Activity, private var pcPosType: PcPosType) : PosSdk, BroadcastReceiver() {
    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.pec.ThirdCompany")
        activity.registerReceiver(this, intentFilter)
    }

    override fun destroy() {
        activity.unregisterReceiver(this)
    }
    var printEnd : ((List<Any>) -> Unit)? = null
    override fun print(byteArray: ByteArray, printEnd: (List<Any>) -> Unit) {
        this.printEnd = printEnd
        try {

            saveFile(byteArray, "PECCO","0")
            createEmptyFile()

            val intent = Intent(Tags.ACTION)
            intent.putExtra(Tags.COMPANY_NAME, "PECCO")
            intent.putExtra(PrintType.PRINT_TYPE, PrintType.RECEIPT_BITMAP)
            intent.putExtra(Tags.NUMBER_OF_BITMAP, 2)
            activity.startActivity(intent);
        }
        catch (e:Exception)
        {
            Log.d("PARSIAN PRINT",e.toString())
        }
    }

    private fun saveFile(byteArray: ByteArray, companyName: String,name:String){

            // don't change this path name
            var savePath = "/sdcard/companies/"
            savePath += "$companyName/"
            var file = File(savePath)
            if (!file.exists()) file.mkdirs()

            val filename = "$savePath/pic$name.bmp"
            file = File(filename)
            if (!file.exists()) {
                if (file.createNewFile()) {
                    val fileos = FileOutputStream(filename)
                    fileos.write(byteArray)
                    fileos.flush()
                    fileos.close()
                }

            }
            else{
                val fileos = FileOutputStream(filename)
                fileos.write(byteArray)
                fileos.flush()
                fileos.close()
            }
    }

    private  fun createEmptyFile(){
        val conf = Bitmap.Config.ARGB_8888 // see other conf types
        val stream = ByteArrayOutputStream()

        val bitmap = Bitmap.createBitmap(22, 70, conf)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        saveFile(stream.toByteArray(),"PECCO","1")
        stream.flush()
        stream.close()
    }


    override fun getPrinterStatus(): PrinterStatus = PrinterStatus.Ok

    var purchaseResultCallback :((PurchaseResultType, Map<String,Any?>) -> Unit)? = null
    var amount :String = "0"
    override fun purchase(amount: String, invoiceNumber: String, purchaseResultCallback: (PurchaseResultType, Map<String, Any?>) -> Unit) {
        this.purchaseResultCallback = purchaseResultCallback
        this.amount = amount
        if(!activity.isPackageInstalled("com.pec.smartpos"))
        {
            var resultJson = hashMapOf<String,Any?>(
                    "status" to 0,
                    "statusDescription" to "",
                    "errorDescription" to "پارسیان نصب نشده است"
            )
            purchaseResultCallback.invoke(PurchaseResultType.InitializationFailed , resultJson)
        }
        else{

            val purchasePayment = Intent(Tags.ACTION)
            purchasePayment.putExtra(Tags.COMPANY_NAME, "PECCO")
            purchasePayment.putExtra(TransactionType.TRANSACTION_TYPE, TransactionType.SALE)
            purchasePayment.putExtra(Tags.AMOUNT,amount)

            activity.startActivity(purchasePayment)

        }
    }


    //this is not used f
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =false
    override fun onReceive(context: Context, intent: Intent) {
        var printStatus = intent.getStringExtra(Response.PRINT_STATUS)
        if(printStatus != null){
            if(printStatus == "FINISHED")
            {
                this.printEnd?.invoke(emptyList())
            }
        }
        else {
            var resultCode = intent.getStringExtra(Response.RESPONSE_CODE)
            if(resultCode != null)
            {
                val response = Response(intent)
                if(response.responseCode == "05")
                {
                    purchaseResultCallback?.invoke(PurchaseResultType.Cancelled,emptyMap<String,Any?>())
                }
                else if(response.responseCode == "00" || response.responseCode == "08")
                {
                    var resultJson = mapOf(
                            Pair("terminalNo",response.terminalNumber),
                            Pair("traceNumber",response.serialNumber),
                            Pair("referenceNo",response.referenceNumber),
                            Pair("amount",amount),
                            Pair("maskedPan",response.cardNumber),
                            Pair("pcPosId",pcPosType.pcPosId),
                            Pair("creditTypeId",pcPosType.creditTypeId)
                    )

                    purchaseResultCallback?.invoke(PurchaseResultType.Succeed,resultJson);
                }

                else{
                    var resultJson = mapOf(
                            Pair("errorCode",response.responseCode),
                            Pair("errorDescription",response.getMessage())
                    )

                    purchaseResultCallback?.invoke(PurchaseResultType.Failed,resultJson)
                }
            }
        }


    }
}

