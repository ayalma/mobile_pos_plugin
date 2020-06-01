import 'dart:async';

import 'package:flutter/services.dart';

class MobilePosPlugin {
  static const MethodChannel _channel =
      const MethodChannel('mobile_pos_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
