import 'package:mobile_pos_plugin/src/host_app.dart';

class PaymentInitializationFailed {
  final int status;
  final String statusDescription;
  final String reserveNumber;
  final String maskedPan;
  final String panHash;
  final String errorDescription;

  PaymentInitializationFailed(
      {this.status,
      this.statusDescription,
      this.reserveNumber,
      this.maskedPan,
      this.panHash,
      this.errorDescription});

  factory PaymentInitializationFailed._fromFANAVAList(List<dynamic> objects) =>
      PaymentInitializationFailed(
          reserveNumber: objects[0],
          maskedPan: objects[1],
          errorDescription: objects[2]);

  factory PaymentInitializationFailed.fromList(HostApp hostApp, objects) {
    switch (hostApp) {
      case HostApp.FANAVA:
      case HostApp.SEP:
      case HostApp.PEC:
        return PaymentInitializationFailed._fromFANAVAList(objects);
        break;
      case HostApp.IKC:
        // TODO: Handle this case.
        break;
      case HostApp.NAVACO:
        // TODO: Handle this case.
        break;
      case HostApp.SEPEHR:
        // TODO: Handle this case.
        break;
      case HostApp.UNKNOWN:
        // TODO: Handle this case.
        break;
    }
    return null;
  }
}

class PaymentCancelled {
  final String reserveNumber;
  final String maskedPan;

  PaymentCancelled({this.reserveNumber, this.maskedPan});

  factory PaymentCancelled._fromFANAVAList(List<dynamic> objects) =>
      PaymentCancelled(reserveNumber: objects[0], maskedPan: objects[1]);

  factory PaymentCancelled.fromList(HostApp hostApp, objects) {
    switch (hostApp) {
      case HostApp.FANAVA:
      case HostApp.SEP:
      case HostApp.PEC:
        return PaymentCancelled._fromFANAVAList(objects);
        break;
      case HostApp.IKC:
        // TODO: Handle this case.
        break;
      case HostApp.NAVACO:
        // TODO: Handle this case.
        break;

      case HostApp.SEPEHR:
        // TODO: Handle this case.
        break;
      case HostApp.UNKNOWN:
        // TODO: Handle this case.
        break;
    }
    return null;
  }
}

class PaymentFailed {
  final int errorCode;
  final String errorDescription;
  final String terminalNo;
  final String merchantId;
  final String posSerial;
  final String reserveNumber;
  final String traceNumber;
  final String rrn;
  final String ref;
  final String amount;
  final String txnDate;
  final String txnTime;
  final String maskedPan;

  PaymentFailed({
    this.errorCode,
    this.errorDescription,
    this.terminalNo,
    this.merchantId,
    this.posSerial,
    this.reserveNumber,
    this.traceNumber,
    this.rrn,
    this.ref,
    this.amount,
    this.txnDate,
    this.txnTime,
    this.maskedPan,
  });

  factory PaymentFailed._fromFANAVAList(List<dynamic> objects) => PaymentFailed(
        errorCode: objects[0],
        errorDescription: objects[1],
        terminalNo: objects[2],
        merchantId: objects[3],
        posSerial: objects[4],
        reserveNumber: objects[5],
        traceNumber: objects[6],
        rrn: objects[7],
        ref: objects[8],
        amount: objects[9],
        txnDate: objects[10],
        txnTime: objects[11],
        maskedPan: objects[12],
      );

  factory PaymentFailed.fromList(HostApp hostApp, objects) {
    switch (hostApp) {
      case HostApp.FANAVA:
      case HostApp.SEP:
      case HostApp.PEC:
        return PaymentFailed._fromFANAVAList(objects);
        break;
      case HostApp.IKC:
        // TODO: Handle this case.
        break;
      case HostApp.NAVACO:
        // TODO: Handle this case.
        break;
      case HostApp.SEPEHR:
        // TODO: Handle this case.
        break;
      case HostApp.UNKNOWN:
        // TODO: Handle this case.
        break;
    }
    return null;
  }
}

class PaymentSucceed {
  final String terminalNo;
  final String merchantId;
  final String posSerial;
  final String reserveNumber;
  final String traceNumber;
  final String rrn;
  final String ref;
  final String amount;
  final String txnDate;
  final String txnTime;
  final String maskedPan;

  PaymentSucceed({
    this.terminalNo,
    this.merchantId,
    this.posSerial,
    this.reserveNumber,
    this.traceNumber,
    this.rrn,
    this.ref,
    this.amount,
    this.txnDate,
    this.txnTime,
    this.maskedPan,
  });

  factory PaymentSucceed._fromFANAVAList(List<dynamic> objects) =>
      PaymentSucceed(
        terminalNo: objects[0],
        merchantId: objects[1],
        posSerial: objects[2],
        reserveNumber: objects[3],
        traceNumber: objects[4],
        rrn: objects[5],
        ref: objects[6],
        amount: objects[7],
        txnDate: objects[8],
        txnTime: objects[9],
        maskedPan: objects[10],
      );

  factory PaymentSucceed.fromList(HostApp hostApp, objects) {
    switch (hostApp) {
      case HostApp.FANAVA:
      case HostApp.SEP:
      case HostApp.PEC:
        return PaymentSucceed._fromFANAVAList(objects);
        break;
      case HostApp.IKC:
        // TODO: Handle this case.
        break;
      case HostApp.NAVACO:
        // TODO: Handle this case.
        break;
      case HostApp.SEPEHR:
        // TODO: Handle this case.
        break;
      case HostApp.UNKNOWN:
        // TODO: Handle this case.
        break;
    }
    return null;
  }
}
