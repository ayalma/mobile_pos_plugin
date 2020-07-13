package com.ayalma.mobile_pos_plugin

import android.app.Activity
import android.content.Intent
import android.device.PrinterManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.NonNull
import com.ayalma.mobile_pos_plugin.posSdk.PosSdk
import com.ayalma.mobile_pos_plugin.posSdk.PosSdkFactory
import com.ayalma.mobile_pos_plugin.posSdk.PurchaseResultType
import com.ayalma.mobile_pos_plugin.posSdk.SdkType
import com.ayalma.mobile_pos_plugin.print.FactorPrintableData
import com.kishcore.sdk.hybrid.api.DataCallback
import com.kishcore.sdk.hybrid.api.HostApp
import com.kishcore.sdk.hybrid.api.SDKManager
import com.kishcore.sdk.sep.rahyab.api.PaymentCallback
import com.kishcore.sdk.sepehr.rahyab.data.PurchaseType
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
/** FlutterHybridCpPlugin */
public class MobilePosPlugin : FlutterPlugin, MethodCallHandler, ActivityAware{
    /**
     * You can registrar
     * */
    var registrar: Registrar? = null
    var binding:ActivityPluginBinding?=null

    var activity: Activity? = null
    var channel: MethodChannel? = null
    var posSdk:PosSdk?=null;



    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        private const val INIT = "init"
        private const val OPEN_MAGNETIC_CARD_READER = "openMagneticStripeCardReader"
        private const val OPEN_BARCODE_SCANNER = "openBarcodeScanner"
        private const val GET_PRINTER_STATUS = "getPrinterStatus"
        private const val PRINTER_PRINT = "printer_print"
        private const val PURCHASE = "purchase"

        private const val OPEN_MAGNETIC_CARD_READER_CALLBACK = "open_magnetic_card_reader_callback"
        private const val OPEN_BARCODE_SCANNER_SUCCESS_CALLBACK = "openBarcodeScannerSuccessCallback"
        private const val OPEN_BARCODE_SCANNER_FAILURE_CALLBACK = "openBarcodeScannerFailureCallback"
        private const val PRINTER_PRINT_CALLBACK = "printer_print_callback"
        private const val PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED = "onPaymentInitializationFailed"
        private const val PURCHASE_ON_PAYMENT_CANCELLED = "onPaymentCancelled"
        private const val PURCHASE_ON_PAYMENT_SUCCEED = "onPaymentSucceed"
        private const val PURCHASE_ON_PAYMENT_FAILED = "onPaymentFailed"
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val instance = MobilePosPlugin()
            instance.registrar = registrar
            instance.activity = registrar.activity()
            instance.onAttachedToEngine(registrar.messenger())
        }
    }

    override fun onDetachedFromActivity() {
        activity = null
        binding = null
        Log.d("Hybrid", "on detached form activity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.binding = binding
        activity = binding.activity;
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.binding = binding
        activity = binding.activity;
        Log.d("Hybrid", "on Attached to activity")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.d("Hybrid", "on detached from activity for config changes")
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.binaryMessenger)
    }

    fun onAttachedToEngine(messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, "mobile_pos_plugin")
        channel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }


    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) = when (call.method) {
        INIT -> init(result,(call.arguments as List<String>)[0])
        OPEN_MAGNETIC_CARD_READER -> openMagneticStripeCardReader(result)
        OPEN_BARCODE_SCANNER -> openBarcodeScanner(result)
        GET_PRINTER_STATUS -> getPrinterStatus(result)
        PRINTER_PRINT -> print(result, call.arguments as ByteArray)
        PURCHASE -> purchase(result, call.arguments as List<String>)
        else -> result.notImplemented()
    }

    private fun init(result: Result, sdkType:String) {
        (activity?.let {
            posSdk = PosSdkFactory.create(SdkType.valueOf(sdkType),it);
            registrar?.addActivityResultListener(posSdk);
            binding?.addActivityResultListener(posSdk as PluginRegistry.ActivityResultListener)
            result.success(HostApp.UNKNOWN.name)
        }
                ?: run {
                    result.error("Activity is null", null, null)
                })
    }
    private fun purchase(result: Result, args: List<String>) {
        var invoiceNumber = args[0]
        var amount = args[1]

        posSdk?.purchase(amount,invoiceNumber) { type: PurchaseResultType, data: List<Any> ->

            val methodName = when(type){
                PurchaseResultType.InitializationFailed->PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED
                PurchaseResultType.Cancelled->PURCHASE_ON_PAYMENT_CANCELLED
                PurchaseResultType.Succeed->PURCHASE_ON_PAYMENT_SUCCEED
                else -> PURCHASE_ON_PAYMENT_FAILED
            }
            channel?.invokeMethod(methodName, data)
        }

        result.success(true);
    }

    private  fun print(result: Result, bytes: ByteArray) {

        posSdk?.print(bytes) { data->
            channel?.invokeMethod(PRINTER_PRINT_CALLBACK, data)
        }
        result.success(true)
    }

    private fun getPrinterStatus(result: Result) =
            result.success(posSdk?.getPrinterStatus().toString())

    private fun openBarcodeScanner(result: Result) =
            activity?.let {
                SDKManager.openBarcodeScanner(true, true, { objs ->
                    channel?.invokeMethod(OPEN_BARCODE_SCANNER_SUCCESS_CALLBACK, objs.toList())
                    val data = objs[0] as String
                    // Tools.displaySafeDialog(this@MainActivity, AnnounceDialog(this@MainActivity, "SCANNED BARCODE:$data", null, 0, null), null)

                }, { data ->
                    channel?.invokeMethod(OPEN_BARCODE_SCANNER_FAILURE_CALLBACK, data.toList())
                    /*    val ret = data[0] as Int
                        if (ret == SDKManager.TIMEOUT) {
                            Tools.displaySafeDialog(this@MainActivity,
                                    AnnounceDialog(this@MainActivity,
                                            "زمان اسکن پایان یافت.", null, 0, null), null)
                        } else if (ret == SDKManager.DEVICE_USED) {
                            Tools.displaySafeDialog(this@MainActivity,
                                    AnnounceDialog(this@MainActivity,
                                            "اسکنر مشغول است. لطفا مجددا تلاش نمایید.", null, 0, null), null)
                        } else {
                            Tools.displaySafeDialog(this@MainActivity,
                                    AnnounceDialog(this@MainActivity,
                                            "خطا در اسکن. لطفا مجددا تلاش نمایید.", null, 0, null), null)
                        }*/
                })

            }
                    ?: run {
                        result.error("activity is null", null, null);
                    }


    private fun openMagneticStripeCardReader(result: Result) {
        activity?.let { activity ->
            SDKManager.openMagneticStripeCardReader(activity, { data ->

                channel?.invokeMethod(OPEN_MAGNETIC_CARD_READER_CALLBACK, data.toList())

            }, { data ->
                Log.d("test", data.toString())
            })
            result.success(true)
        }
                ?: run {
                    result.error("activity is null", null, null)
                }
    }



}