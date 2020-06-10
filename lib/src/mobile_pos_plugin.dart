import 'dart:async';
import 'dart:typed_data';

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
  OpenBarcodeScannerSuccessCallback _barcodeScannerSuccessCallback;
  OpenBarcodeScannerFailureCallback _barcodeScannerFailureCallback;
  PrinterPrintCallback _printerPrintCallback;
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

  Completer<List<String>> _magnetCardComplator;
  Future<List<String>> openMagneticStripeCardReader() async {
    _magnetCardComplator = Completer();
    try {
      final bool result =
          await _methodChannel.invokeMethod(_OPEN_MAGNETIC_CARD_READER);
      if (result == false) {
        _magnetCardComplator.completeError('خطا در بازکردن کارت ریدر');
      }
    } catch (error) {
      _magnetCardComplator.completeError(error);
    }

    return _magnetCardComplator.future;
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

  @Deprecated('Use printAsync')
  Future<bool> print(Uint8List image, PrinterPrintCallback printCallback) {
    _printerPrintCallback = printCallback;
    return _methodChannel.invokeMethod(_PRINTER_PRINT, image);
  }

  Completer<List<dynamic>> printComplater;
  Future<List<dynamic>> printAsync(Uint8List image) async {
    printComplater = Completer();
    final result = await _methodChannel.invokeMethod(_PRINTER_PRINT, image);
    if (!result) printComplater.completeError('Error happend');
    return printComplater.future;
  }

  Completer<PaymentResult> _purchaseComplator;
  Future<PaymentResult> purchase(
    String invoiceNumber,
    String amount,
    HostApp hostApp,
  ) {
    _purchaseComplator = Completer();
    _methodChannel
        .invokeMethod(_PURCHASE, [invoiceNumber, amount, hostApp.toString()]);
    return _purchaseComplator.future;
  }

  Future _methodHandler(MethodCall call) {
    switch (call.method) {
      case _OPEN_MAGNETIC_CARD_READER_CALLBACK:
        var args = call.arguments;
        _magnetCardComplator.complete([args[0] as String, args[1] as String]);
        return Future.value();
      case _OPEN_BARCODE_SCANNER_SUCCESS_CALLBACK:
        return _barcodeScannerSuccessCallback(call.arguments);
      case _OPEN_BARCODE_SCANNER_FAILURE_CALLBACK:
        return _barcodeScannerFailureCallback(call.arguments);
      case _PRINTER_PRINT_CALLBACK:
        printComplater.complete(call.arguments);
        if (_printerPrintCallback != null)
          return _printerPrintCallback(call.arguments);
        return Future.value();
      case _PURCHASE_ON_PAYMENT_INITIALIZATION_FAILED:
        _purchaseComplator.complete(
            PaymentInitializationFailed.fromList(_hostApp, call.arguments));
        return Future.value();
      case _PURCHASE_ON_PAYMENT_CANCELLED:
        _purchaseComplator
            .complete(PaymentCancelled.fromList(_hostApp, call.arguments));
        return Future.value();
      case _PURCHASE_ON_PAYMENT_FAILED:
        _purchaseComplator
            .complete(PaymentFailed.fromList(_hostApp, call.arguments));
        return Future.value();
      case _PURCHASE_ON_PAYMENT_SUCCEED:
        _purchaseComplator
            .complete(PaymentSucceed.fromList(_hostApp, call.arguments));
        return Future.value();
      default:
        return Future.error('method not defined');
    }
  }
}
