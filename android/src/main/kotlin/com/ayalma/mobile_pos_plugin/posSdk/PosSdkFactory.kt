package com.ayalma.mobile_pos_plugin.posSdk

import android.app.Activity
import com.ayalma.mobile_pos_plugin.posSdk.Parsian.ParsianPosSdk

object PosSdkFactory {
    fun create(type: PcPosType,activity: Activity): PosSdk? {
        return when(type) {
            PcPosType.Rahyab -> RahyabPosSdk(activity,type)
            PcPosType.Parsian-> ParsianPosSdk(activity,type)

            PcPosType.Pne -> PnePosSdk(activity,type)
            PcPosType.Saman -> PnePosSdk(activity,type)
            else -> null
        }
    }
}
