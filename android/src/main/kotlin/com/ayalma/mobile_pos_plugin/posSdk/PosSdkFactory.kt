package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity

object PosSdkFactory {
    fun create(type: SdkType,activity: Activity): PosSdk? {
        return when(type) {
            SdkType.Rahyab -> RahyabPosSdk(activity)
            SdkType.Pne -> PnePosSdk(activity)
            else -> null
        }
    }
}
