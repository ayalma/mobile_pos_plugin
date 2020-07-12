package com.ayalma.mobile_pos_plugin.posSdk

import io.flutter.plugin.common.PluginRegistry

interface PosSdk: PluginRegistry.ActivityResultListener {
    /***
     * print
     */
    fun print(byteArray:ByteArray,printEnd:(List<Any>)->Unit)
}