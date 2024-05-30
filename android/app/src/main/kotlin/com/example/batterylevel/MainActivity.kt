package com.example.batterylevel
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    companion object {
        private const val CHANNEL = "samples.flutter.dev/battery"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getNativeData" -> {
                    val data = getNativeData() // Implement this method to use your .aar functionality
                    result.success(data)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun getNativeData(): String {
        // Access your library's functionality here and return data
        return "Data from native code"
    }
}
