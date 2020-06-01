import 'dart:async';
import 'dart:typed_data';
import 'package:image/image.dart';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:mobile_pos_plugin/src/host_app.dart';
import 'package:mobile_pos_plugin/src/payment.dart';

typedef OpenMagnetCallback = Future<void> Function(List<String>);
typedef OpenBarcodeScannerSuccessCallback = Future<void> Function(
    List<dynamic>);
typedef OpenBarcodeScannerFailureCallback = Future<void> Function(
    List<dynamic>);

typedef PrinterPrintCallback = Future<void> Function(List<dynamic>);

typedef PaymentCancelledCallback = Future<void> Function(PaymentCancelled);
typedef PaymentInitializationFailedCallback = Future<void> Function(
    PaymentInitializationFailed);
typedef PaymentFailedCallback = Future<void> Function(PaymentFailed);
typedef PaymentSucceedCallback = Future<void> Function(PaymentSucceed);

class MobilePosPlugin {
  static const String _INIT = "init";
  static const String _OPEN_MAGNETIC_CARD_READER =
      "openMagneticStripeCardReader";
  static const String _OPEN_BARCODE_SCANNER = "openBarcodeScanner";
  static const String _GET_PRINTER_STATUS = "getPrinterStatus";
  static const String _PRINTER_PRINT = "printer_print";
  static const String _PURCHASE = "purchase";

  ///callback strings
  static const String _OPEN_MAGNETIC_CARD_READER_CALLBACK =
      "open_magnetic_card_reader_callback";

  static const String _OPEN_BARCODE_SCANNER_SUCCESS_CALLBACK =
      "openBarcodeScannerSuccessCallback";
  static const String _OPEN_BARCODE_SCANNER_FAILURE_CALLBACK =
      "openBarcodeScannerFailureCallback";
  static const String _PRINTER_PRINT_CALLBACK = "printer_print_callback";

  static const String _PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED =
      "onPaymentInitializationFailed";
  static const String _PURCHASE_ON_PAYMENT_CANCELLED = "onPaymentCancelled";
  static const String _PURCHASE_ON_PAYMENT_SUCCEED = "onPaymentSucceed";
  static const String _PURCHASE_ON_PAYMENT_FAILED = "onPaymentFailed";

  factory MobilePosPlugin() {
    if (_instance == null) {
      final MethodChannel methodChannel =
          const MethodChannel('mobile_pos_plugin');

      /* final EventChannel eventChannel =
      const EventChannel('plugins.flutter.io/charging');*/
      _instance = MobilePosPlugin.private(methodChannel);
    }
    return _instance;
  }

  /// This constructor is only used for testing and shouldn't be accessed by
  /// users of the plugin. It may break or change at any time.
  @visibleForTesting
  MobilePosPlugin.private(this._methodChannel) {
    _methodChannel.setMethodCallHandler(_methodHandler);
  }

  static MobilePosPlugin _instance;

  final MethodChannel _methodChannel;

  OpenMagnetCallback _magnetCallback;
  OpenBarcodeScannerSuccessCallback _barcodeScannerSuccessCallback;
  OpenBarcodeScannerFailureCallback _barcodeScannerFailureCallback;
  PrinterPrintCallback _printerPrintCallback;
  PaymentInitializationFailedCallback _paymentInitializationFailedCallback;
  PaymentCancelledCallback _paymentCancelledCallback;
  PaymentFailedCallback _paymentFailedCallback;
  PaymentSucceedCallback _paymentSucceedCallback;

  HostApp _hostApp;

  ///
  /// with this method you will init the payment lib
  ///
  Future<HostApp> init() async {
    _hostApp = await _methodChannel
        .invokeMethod(_INIT)
        .then((hostName) => parseHostApp(hostName));
    return _hostApp;
  }

  ///
  /// this method make magnet card reader active
  ///
  Future<bool> openMagneticStripeCardReader(
      OpenMagnetCallback openMagnetCallback) async {
    _magnetCallback = openMagnetCallback;
    final bool result =
        await _methodChannel.invokeMethod(_OPEN_MAGNETIC_CARD_READER);
    return result;
  }

  ///
  /// open barcode scanner and wait for result with callback
  ///
  Future<bool> openBarcodeScanner(
      OpenBarcodeScannerSuccessCallback openBarcodeScannerSuccessCallback,
      OpenBarcodeScannerFailureCallback openBarcodeScannerFailureCallback) {
    this._barcodeScannerFailureCallback = openBarcodeScannerFailureCallback;
    this._barcodeScannerSuccessCallback = openBarcodeScannerSuccessCallback;
    return _methodChannel.invokeMethod(_OPEN_BARCODE_SCANNER);
  }

  ///
  /// get printer status
  /// this method will return an integer number for checking status you must compare it to constant from [constant] class
  ///
  Future<int> getPrinterStatus() =>
      _methodChannel.invokeMethod(_GET_PRINTER_STATUS);

  Future<bool> print(Uint8List image, PrinterPrintCallback printCallback) {
    _printerPrintCallback = printCallback;
    return _methodChannel.invokeMethod(_PRINTER_PRINT, image);
  }

  Future<bool> purchase(
    String invoiceNumber,
    String amount,
    HostApp hostApp,
    PaymentInitializationFailedCallback paymentInitializationFailedCallback,
    PaymentCancelledCallback paymentCancelledCallback,
    PaymentFailedCallback paymentFailedCallback,
    PaymentSucceedCallback paymentSucceedCallback,
  ) {
    _paymentInitializationFailedCallback = paymentInitializationFailedCallback;
    _paymentCancelledCallback = paymentCancelledCallback;
    _paymentFailedCallback = paymentFailedCallback;
    _paymentSucceedCallback = paymentSucceedCallback;

    return _methodChannel
        .invokeMethod(_PURCHASE, [invoiceNumber, amount, hostApp.toString()]);
  }

  Future _methodHandler(MethodCall call) {
    switch (call.method) {
      case _OPEN_MAGNETIC_CARD_READER_CALLBACK:
        var args = call.arguments;
        return _magnetCallback([args[0] as String, args[1] as String]);
        break;
      case _OPEN_BARCODE_SCANNER_SUCCESS_CALLBACK:
        return _barcodeScannerSuccessCallback(call.arguments);
      case _OPEN_BARCODE_SCANNER_FAILURE_CALLBACK:
        return _barcodeScannerFailureCallback(call.arguments);
      case _PRINTER_PRINT_CALLBACK:
        return _printerPrintCallback(call.arguments);
      case _PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED:
        return _paymentInitializationFailedCallback(
            PaymentInitializationFailed.fromList(_hostApp, call.arguments));
      case _PURCHASE_ON_PAYMENT_CANCELLED:
        return _paymentCancelledCallback(
            PaymentCancelled.fromList(_hostApp, call.arguments));
      case _PURCHASE_ON_PAYMENT_FAILED:
        return _paymentFailedCallback(
            PaymentFailed.fromList(_hostApp, call.arguments));
      case _PURCHASE_ON_PAYMENT_SUCCEED:
        return _paymentSucceedCallback(
            PaymentSucceed.fromList(_hostApp, call.arguments));
      default:
        return Future.error('method not defined');
    }
  }
}
