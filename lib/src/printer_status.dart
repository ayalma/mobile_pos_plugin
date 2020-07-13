enum PrinterStatus {
  NoPaper,
  Ok,
  Unknown,
  OverHeat,
  LowVol,
  PaperJam,
  Busy,
  LiftHead,
  CutPositionError,
  LowTemp
}

PrinterStatus parsePrinterStatus(String printerStatus) {
  return PrinterStatus.values.firstWhere(
      (ps) => ps.toString() == 'PrinterStatus.$printerStatus',
      orElse: () => PrinterStatus.Unknown);
}
