// package com.oriserve.oriwebchatbot;
package com.example.batterylevel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity

import com.oriserve.orichat.OriChatBot
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList



class MainActivity: FlutterActivity() {
    companion object {
        private const val CHANNEL = "samples.flutter.dev/battery"
        private lateinit var oriChatBot: OriChatBot
        private lateinit var userData: JSONObject
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Your code here
        if(!OriChatBot.isAvailable()) {
        } else{
            oriChatBot =  OriChatBot(this);
           println("OriChatBot is available"+this);
                // bot.showToolbar(showToolbar.isChecked());
                // bot.showMiniToolbar(showMiniToolbar.isChecked()).setPageTitle(editText1.getText().toString());
                // String VC = editText.getText().toString().trim();
                // String baseUrl = baseUrltext.getText().toString().trim();
                userData = JSONObject();
                    userData.put("udf1" , "9734902237");
                    userData.put("udf3" , "");
                    userData.put("customerId" , "1000003402");
                    userData.put("orderId" , "");
                    userData.put("orderLineItemId" , "vodafone-dev");
                    userData.put("journeyId" , "helpAndSupport-Anand");
                    userData.put("brand" , "vodafone-dev");
                    userData.put("appDetails", "8.0.4");
                    userData.put("redirectionType", "internal");
                    oriChatBot.setPageTitle("Vodafone");
                    oriChatBot.setAppDetails("https://vil-dev.oriserve.in/androidAppVodafone.html");
                    oriChatBot.setUserData(userData.toString());
                    // oriChatBot.setBotIdentifier("vodafone-dev");
                //     oriChatBot.setBrand(brand);
                //     oriChatBot.setMSISDN("9734902237");
                //     oriChatBot.setStateCode(stateCode);
                    // oriChatBot.setCustomerName(customerName);
                //     oriChatBot.setLob(lob);
                //     oriChatBot.setJourneyData(journeyData.toString());
                //     oriChatBot.addEventListener(new OriChatBot.EventListener() {
                //     @Override
                //     public void onEventReceived(JSONObject data) {
                //         System.out.println("Event Received :------------------->  " + data);
                //     }
                // });
                // oriChatBot.setBotId("https://vil-dev.oriserve.in/");
                oriChatBot.launchBot();
        }

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
        return "OriChatBot is available"
        // // Access your library's functionality here and return data
        // if(!OriChatBot.isAvailable()) {
        //     return "OriChatBot is not available"
        // } else{
        //     oriChatBot =  OriChatBot(this);
        //    println("OriChatBot is available"+this);
        //         // bot.showToolbar(showToolbar.isChecked());
        //         // bot.showMiniToolbar(showMiniToolbar.isChecked()).setPageTitle(editText1.getText().toString());
        //         // String VC = editText.getText().toString().trim();
        //         // String baseUrl = baseUrltext.getText().toString().trim();
        //         userData = JSONObject();
        //             userData.put("udf1" , "9734902237");
        //             userData.put("udf3" , "");
        //             userData.put("customerId" , "1000003402");
        //             userData.put("orderId" , "");
        //             userData.put("orderLineItemId" , "vodafone-dev");
        //             userData.put("journeyId" , "helpAndSupport-Anand");
        //             userData.put("brand" , "vodafone-dev");
        //             userData.put("appDetails", "8.0.4");
        //             userData.put("redirectionType", "internal");
        //             oriChatBot.setPageTitle("Vodafone");
        //             // oriChatBot.setAppDetails("https://vil-dev.oriserve.in/androidAppVodafone.html");
        //             oriChatBot.setUserData(userData.toString());
        //             // oriChatBot.setBotIdentifier("vodafone-dev");
        //         //     oriChatBot.setBrand(brand);
        //         //     oriChatBot.setMSISDN("9734902237");
        //         //     oriChatBot.setStateCode(stateCode);
        //             // oriChatBot.setCustomerName(customerName);
        //         //     oriChatBot.setLob(lob);
        //         //     oriChatBot.setJourneyData(journeyData.toString());
        //         //     oriChatBot.addEventListener(new OriChatBot.EventListener() {
        //         //     @Override
        //         //     public void onEventReceived(JSONObject data) {
        //         //         System.out.println("Event Received :------------------->  " + data);
        //         //     }
        //         // });
        //         // oriChatBot.setBotId("https://vil-dev.oriserve.in/");
        //         oriChatBot.launchBot();
        //         return "OriChatBot is available";

        // }
    }
}
