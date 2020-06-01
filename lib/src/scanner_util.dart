// left scan keyCode
import 'package:flutter/services.dart';

const int LEFT_SCAN_KEY = 132;
// right scan keyCode
const int RIGHT_SCAN_KEY = 133;
// scan keyCode
const int SCAN_KEY = 165;
// bluetooth scan keyCode (i'm not test)
const int BLUETOOTH_SCAN_KEY = 113;

bool isOnScan(int keyCode) {
  return keyCode == LEFT_SCAN_KEY ||
      keyCode == RIGHT_SCAN_KEY ||
      keyCode == SCAN_KEY ||
      keyCode == BLUETOOTH_SCAN_KEY;
}