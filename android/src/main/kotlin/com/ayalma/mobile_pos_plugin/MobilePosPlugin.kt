package com.ayalma.mobile_pos_plugin

import android.app.Activity
import android.device.PrinterManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.NonNull
import com.ayalma.mobile_pos_plugin.print.FactorPrintableData
import com.kishcore.sdk.hybrid.api.DataCallback
import com.kishcore.sdk.hybrid.api.HostApp
import com.kishcore.sdk.hybrid.api.SDKManager
import com.kishcore.sdk.sep.rahyab.api.PaymentCallback
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
/** FlutterHybridCpPlugin */
public class MobilePosPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    var activity: Activity? = null
    var channel: MethodChannel? = null
    var printerManager:PrinterManager? = null;
    var sdkType:SdkType = SdkType.Unknown;



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
            instance.activity = registrar.activity()
            instance.onAttachedToEngine(registrar.messenger())
        }
    }

    override fun onDetachedFromActivity() {
        activity = null
        Log.d("Hybrid", "on detached form activity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d("Hybrid", "on Reattached to activity for config changes")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
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
        INIT -> init(result)
        OPEN_MAGNETIC_CARD_READER -> openMagneticStripeCardReader(result)
        OPEN_BARCODE_SCANNER -> openBarcodeScanner(result)
        GET_PRINTER_STATUS -> getPrinterStatus(result)
        PRINTER_PRINT -> print(result, call.arguments as ByteArray)
        PURCHASE -> purchase(result, call.arguments as List<String>)
        else -> result.notImplemented()
    }

    private fun init(result: Result) {
        (activity?.let {
            val hostApp = SDKManager.init(activity)
            if(hostApp != HostApp.UNKNOWN)
            {
                sdkType = SdkType.Rahyab;
                result.success(hostApp.name)
            }
            else{
                sdkType = SdkType.Rahyab;
                printerManager = PrinterManager();
                printerManager?.setupPage(384, -1)
            }



        }
                ?: run {
                    result.error("Activity is null", null, null)
                })
    }
    private fun purchase(result: Result, args: List<String>) {
        var invoiceNumber = args[0]
        var amount = args[1]
        var hostApp = args[2]
        activity?.let {
            if (hostApp == "HostApp.FANAVA") {
                com.kishcore.sdk.fanava.rahyab.api.SDKManager.purchase(it, invoiceNumber, amount, object : com.kishcore.sdk.fanava.rahyab.api.PaymentCallback {
                    override fun onPaymentInitializationFailed(reserveNumber: String?, maskedPan: String?, errorDescription: String?,panHash:String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED, arrayListOf(reserveNumber, maskedPan, errorDescription))
                    }

                    override fun onPaymentCancelled(reserveNumber: String?, maskedPan: String?,panHash:String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_CANCELLED, arrayListOf(reserveNumber, maskedPan))
                    }

                    override fun onPaymentSucceed(terminalNo: String?, merchantId: String?, posSerial: String?, reserveNumber: String?, traceNumber: String?, rrn: String?, ref: String?, amount: String?, txnDate: String?, txnTime: String?, maskedPan: String?,panHash:String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_SUCCEED, arrayListOf(terminalNo, merchantId, posSerial, reserveNumber, traceNumber, rrn, ref, amount, txnDate, txnTime, maskedPan))
                    }

                    override fun onPaymentFailed(errorCode: Int, errorDescription: String, terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String,panHash:String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_FAILED, arrayListOf(errorCode, errorDescription, terminalNo, merchantId, posSerial, reserveNumber, traceNumber, rrn, ref, amount, txnDate, txnTime, maskedPan))
                    }
                })
            } else if (hostApp == "HostApp.SEP") {
                com.kishcore.sdk.sep.rahyab.api.SDKManager.purchase(it, invoiceNumber, amount, object : PaymentCallback {
                    override fun onPaymentInitializationFailed(reserveNumber: String?, maskedPan: String?, errorDescription: String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED, arrayListOf(reserveNumber, maskedPan, errorDescription))
                    }

                    override fun onPaymentCancelled(reserveNumber: String?, maskedPan: String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_CANCELLED, arrayListOf(reserveNumber, maskedPan))
                    }

                    override fun onPaymentSucceed(terminalNo: String?, merchantId: String?, posSerial: String?, reserveNumber: String?, traceNumber: String?, rrn: String?, ref: String?, amount: String?, txnDate: String?, txnTime: String?, maskedPan: String?) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_SUCCEED, arrayListOf(terminalNo, merchantId, posSerial, reserveNumber, traceNumber, rrn, ref, amount, txnDate, txnTime, maskedPan))
                    }


                    override fun onPaymentFailed(errorCode: Int, errorDescription: String, terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_FAILED, arrayListOf(errorCode, errorDescription, terminalNo, merchantId, posSerial, reserveNumber, traceNumber, rrn, ref, amount, txnDate, txnTime, maskedPan))
                    }
                })
            } else if (hostApp == "HostApp.PEC") {
                com.kishcore.sdk.parsian.rahyab.api.SDKManager.purchase(it, invoiceNumber, amount, object : com.kishcore.sdk.parsian.rahyab.api.PaymentCallback {
                    override fun onPaymentInitializationFailed(reserveNumber: String, maskedPan: String, errorDescription: String) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED, arrayListOf(reserveNumber, maskedPan, errorDescription))
                    }

                    override fun onPaymentSucceed(terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_SUCCEED, arrayListOf(terminalNo, merchantId, posSerial, reserveNumber, traceNumber, rrn, ref, amount, txnDate, txnTime, maskedPan))
                    }

                    override fun onPaymentFailed(errorCode: Int, errorDescription: String, terminalNo: String, merchantId: String, posSerial: String, reserveNumber: String, traceNumber: String, rrn: String, ref: String, amount: String, txnDate: String, txnTime: String, maskedPan: String) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_FAILED, arrayListOf(errorCode, errorDescription, terminalNo, merchantId, posSerial, reserveNumber, traceNumber, rrn, ref, amount, txnDate, txnTime, maskedPan))

                    }

                    override fun onPaymentCancelled(reserveNumber: String, maskedPan: String) {
                        channel?.invokeMethod(PURCHASE_ON_PAYMENT_CANCELLED, arrayListOf(reserveNumber, maskedPan))
                    }
                })
            }
        }
                ?: run {
                    result.error("activity is null", null, null);
                }

        // channel?.invokeMethod(PURCHASE_CALLBACK,);
        result.success(true);
    }

    private fun print(result: Result, bytes: ByteArray) {
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size);

        printerManager?.drawBitmap(bmp,bmp.width,bmp.height);

//        SDKManager.print(activity, FactorPrintableData(bytes), DataCallback { data ->
//            channel?.invokeMethod(PRINTER_PRINT_CALLBACK, data.toList())
//
//        })
        result.success(true)
    }

    private fun getPrinterStatus(result: Result) =
            result.success(SDKManager.getPrinterStatus())

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

enum class  SdkType{
    Pna,
    Rahyab,
    Unknown,
};
