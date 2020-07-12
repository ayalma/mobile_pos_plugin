package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity
import android.content.Intent
import android.device.PrinterManager
import android.graphics.BitmapFactory
import com.ayalma.mobile_pos_plugin.print.FactorPrintableData
import com.kishcore.sdk.hybrid.api.SDKManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PnePosSdk(private var activity: Activity) : PosSdk {
    var printerManager : PrinterManager = PrinterManager();
    override fun print(byteArray: ByteArray, printEnd: (List<Any>) -> Unit) {
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size);
        printerManager.drawBitmap(bmp,0,0);
        printerManager.printPage(0)
        printerManager.paperFeed(15)
        bmp.recycle()
        printEnd.invoke(ArrayList(0))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        TODO("Not yet implemented")
    }
}