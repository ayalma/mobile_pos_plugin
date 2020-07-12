package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity
import android.content.Intent
import com.ayalma.mobile_pos_plugin.print.FactorPrintableData
import com.kishcore.sdk.hybrid.api.DataCallback
import com.kishcore.sdk.hybrid.api.SDKManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RahyabPosSdk(private var activity: Activity) : PosSdk {
    init {
        SDKManager.init(activity)
    }

    override fun print(byteArray: ByteArray, printEnd: (List<Any>) -> Unit) {
        SDKManager.print(activity, FactorPrintableData(byteArray), DataCallback{ data-> printEnd.invoke(data.toList())})
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        TODO("Not yet implemented")
    }
}