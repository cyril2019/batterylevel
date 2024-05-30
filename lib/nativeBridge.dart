import 'package:flutter/services.dart';

class NativeBridge {
  static const MethodChannel _channel =
      MethodChannel('samples.flutter.dev/battery');

  static Future<String?> getNativeData() async {
    final String? data = await _channel.invokeMethod('getNativeData');
    return data;
  }
}
