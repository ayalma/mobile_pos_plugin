package com.ayalma.mobile_pos_plugin.posSdk

enum class PrinterStatus {
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