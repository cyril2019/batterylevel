package com.oriserve.orichat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * needs VC No passed as string in Intent to this activity
 */
public class OriChatActivity extends AppCompatActivity {

    static final String TAG = "OriChatActivityTAG";
    private WebView webView;
    private String baseUrl = "";
    private String prefsKey = "";
    private String psid = "";
    private boolean webviewSuccess = true;
    private final String JAVASCRIPT_OBJ = "javascript_obj";
    private int refreshCounter = 0;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private static Boolean isInternalRedirection = false;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 123;
    private static final boolean IS_RECORD_AUDIO_PERMISSION = false;

    long lastBackPressed = 0;
    @Override
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR); // to add or remove toolbar
        setContentView(R.layout.activity_ori_chat);

        setUpUI();
        String userData = "";
        String botIdentifier = "";

        try{
            baseUrl = getIntent().getStringExtra("baseUrl");
            userData = getIntent().getStringExtra("userData");
            botIdentifier = getIntent().getStringExtra("botIdentifier");
            if(baseUrl.equalsIgnoreCase("")){
                baseUrl = "https://web.vodafone-elb.oriserve.in/chatbotVodaApp/androidAppVodafone.html";
            }
            // if botIdentifier is not empty, assign baseUrl to the following values
            if(!botIdentifier.equalsIgnoreCase("")){
                if(botIdentifier.equalsIgnoreCase("vodafone-dev")){
                    baseUrl = "https://vil-dev.oriserve.in/androidAppVodafone.html";
                }else if(botIdentifier.equalsIgnoreCase("vodafone-uat")){
                    baseUrl = "https://webtest.vodafone-elb.oriserve.in/chatbot/androidAppVodafone.html";
                }else if(botIdentifier.equalsIgnoreCase("vodafone-prod")){
                    baseUrl = "https://web.vodafone-elb.oriserve.in/chatbotVodaApp/androidAppVodafone.html";
                }else if(botIdentifier.equalsIgnoreCase("marketplace-dev")){
                    baseUrl = "https://vi-marketplace-dev.oriserve.com/chatbot/androidApp.html";
                }else if(botIdentifier.equalsIgnoreCase("marketplace-prod")){
                    baseUrl = "https://vi-marketplace.oriserve.com/chatbot/androidApp.html";
                }
            }
        }catch(Exception e){
            System.out.println("Error- scope : botIdentifierHandling, Error : " + e.getMessage());
        }



        //baseUrl

        try {
            if(!userData.isEmpty()) {
                JSONObject userDataJson = new JSONObject(userData);
                if(userDataJson.has("redirectionType") && userDataJson.getString("redirectionType").equalsIgnoreCase("internal")){
                    isInternalRedirection = true;
                }
            }
        } catch (JSONException e) {
            System.out.println("JSON is invalid, scope : isInternalRedirection, Error : " + e.getMessage());
        }


        final ProgressBar progressBar = findViewById(R.id.loading_indicator);
        final View noInternet = findViewById(R.id.no_internet);
        // overridePendingTransition(0,android.R.anim.slide_out_right);
        webView = findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // if(BuildConfig.xDEBUG) {
        // webView.setWebContentsDebuggingEnabled(true);
        // }
        // webSettings.setSupportMultipleWindows(false);
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // enable local storage
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            File databasePath = getDatabasePath("yourDbName");
            webSettings.setDatabasePath(databasePath.getPath());
        }

        // webSettings.setAllowFileAccessFromFileURLs(true); //don't need this rule
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // need to set origin above jellybean
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        // app cache
        /*
            webSettings.setAppCacheEnabled(true);  commented after migrating from Android X
        */

        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);


        // make sure zoom is disabled
        webSettings.setUseWideViewPort(false);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        // String botUrl = "http://139.59.17.69:8080/?ori_platform=android";
        // String botUrl = "http://127.0.0.1:3000/";
        // String botUrl = "http://10.0.2.2:3000/";
        //dev ur;
        //baseUrl = "https://vil-dev.oriserve.in/androidAppVodafone.html";
        //prod url
        //baseUrl = "https://web.vodafone-elb.oriserve.in/chatbotVodaApp/androidAppVodafone.html";
        //uat url
        //baseUrl = "https://webtest.vodafone-elb.oriserve.in/chatbot/androidAppVodafone.html";
        //AA - dev
//          baseUrl = "https://web.vodafone-elb.oriserve.in/chatbotVodaApp/androidAppVodafone.html";
        //noc - dev
//            baseUrl = "https://vi-noc.oriserve.com/chatbot/androidApp.html";

//       // flynas - dev
        //baseUrl = "https://flynas-dev.oriserve.com/chatbot/androidApp.html";


        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 OriLog(TAG,"Got string from JS: "+url);
                // view.loadUrl(baseUrl);
//                return true;
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if( url.startsWith("http:") || url.startsWith("https:") ) {
                    view.loadUrl(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("mailto:")) {
                    // TODO : handle mail url
                    return true;
                }
                else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Log.i(TAG, "shouldOverrideUrlLoading Exception:" + e);
                        return true;
                    }
                }
            }

            @TargetApi(Build.VERSION_CODES.N)
            // for api 25 and above, this method was introduced and prev one deprecated
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // OriLog(TAG,"Got string from JS: "+request.getUrl().toString());
                // view.loadUrl(request.getUrl().toString());
//                return true;
                if (request.getUrl().toString().startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                    return true;
                } else if( request.getUrl().toString().startsWith("http:") || request.getUrl().toString().startsWith("https:") ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                    return true;
                } else if (request.getUrl().toString().startsWith("mailto:")) {
                    // TODO : handle mail url
                    return true;
                }
                else{
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("request.getUrl().toString()"));
                        view.getContext().startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Log.i(TAG, "shouldOverrideUrlLoading Exception 1:" + e);
                        return true;
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) { // called when webview finished loading
                super.onPageFinished(view, url);
                if (webviewSuccess) {
                    OriLog(TAG, "reached here: Page loaded for url " + url + " base url: " + baseUrl);

                    if (url.equalsIgnoreCase(baseUrl)) {
                        injectJavaScriptFunction();
//                        getConfirmationToCloseWebView();
                    }
                    progressBar.setVisibility(View.GONE);
                    noInternet.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) { // called when webview received any error
                webviewSuccess = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    OriLog(TAG, "Build.VERSION.SDK_INT" + Build.VERSION.SDK_INT);
                    OriLog(TAG, "Build.VERSION.Build.VERSION_CODES.M" + Build.VERSION_CODES.M);
                    OriLog(TAG, "Got an error 9e2892e" + error.getDescription().toString());
                    OriLog(TAG, "Got an error 9e2892e complete" + error);
                } else {
                    OriLog(TAG, "Got an error 89fuiejf93" + error.toString());
                }

                progressBar.setVisibility(View.GONE); // loading view close
                webView.setVisibility(View.GONE); // webview visibility close
                noInternet.setVisibility(View.VISIBLE); // error page will display
                super.onReceivedError(view, request, error);
            }



        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                // OriLog("CONSOLE_LOG", cm.message() + " at " + cm.sourceId() + ":" +
                // cm.lineNumber());
                OriLog(TAG + "_CONSOLE FROM JS", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                try{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (isPermissionGranted()) {
                            // Permission is already granted, proceed with the request.
                            request.grant(request.getResources());
                        } else {
                            requestPermission();
                            request.grant(request.getResources());
                        }
                    }
                }catch(Exception e) {
                    System.out.println("Error Occured RECORD AUDIO, Scope : onPermissionRequest, Error : " + e.getMessage());
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

        });

        webView.addJavascriptInterface(new OriJavaScriptInterface(), JAVASCRIPT_OBJ);

        ImageButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() { // assigning onClick function on refresh button
            @Override
            public void onClick(View v) {
                if (refreshCounter < 3) {
                    refreshCounter++;
                } else {
                    refreshCounter = 0;
                    webView.clearCache(true);
                }
                if (isNetworkAvailable(OriChatActivity.this)) {
                    progressBar.setVisibility(View.VISIBLE);
                    noInternet.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);

                    webviewSuccess = true;
                    webView.loadUrl(baseUrl);
                }
            }
        });

        if (isNetworkAvailable(OriChatActivity.this)) {
            progressBar.setVisibility(View.VISIBLE); // loader will display
            noInternet.setVisibility(View.GONE); // error page will be hidded
            webView.setVisibility(View.GONE); // webview will be hidden


            webviewSuccess = true;
            webView.loadUrl(baseUrl); // call webview to load chatbot
        } else {// if network will not avaliable
            progressBar.setVisibility(View.GONE); // loader will be hidden
            webView.setVisibility(View.GONE); // webview will be hidden
            noInternet.setVisibility(View.VISIBLE); // error page will be visible
        }

    }

    @Override
    protected void onDestroy() {
        webView.removeJavascriptInterface(JAVASCRIPT_OBJ);
        super.onDestroy();
    }
    private static Toast toast;
    @Override
    public boolean onKeyDown(int code, KeyEvent e) {
        Log.d("Webactivity", "Back Pressed");
//        Toast.makeText(this, "Please click on Confirm", Toast.LENGTH_SHORT).show();
        String KeyEventString = "";
        if(e != null){
            KeyEventString = e.toString();
            if(KeyEventString.contains("VOLUME_DOWN") || KeyEventString.contains("VOLUME_UP")){
                return super.onKeyDown(code, e);
            }
        }


        if (isNetworkAvailable(OriChatActivity.this)) {
            injectBackButtonAction();
        }
        else{
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBackPressed > 2000) {
                Toast.makeText(this, "Press Back again to exit..", Toast.LENGTH_SHORT).show();
                lastBackPressed = currentTime;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
                super.onBackPressed();
            }
        }
        if (code == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(code, e);
        }
    }

    @Override
    protected void onUserLeaveHint()
    {
        String key = "appMinimizedfrom";
        String val = "chatbot";
        String chatBotTimeStamp = "chatBotTimeStamp";
        String ts= "" + System.currentTimeMillis() / 1000;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("localStorage.setItem('"+ key +"','"+ val +"');", null);
            webView.evaluateJavascript("localStorage.setItem('"+ chatBotTimeStamp +"','"+ ts +"');", null);
        } else {
            webView.loadUrl("javascript:localStorage.setItem('"+ key +"','"+ val +"');");
            webView.loadUrl("javascript:localStorage.setItem('"+ chatBotTimeStamp +"','"+ ts +"');");
        }
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed() {

//        super.onBackPressed();
        onKeyDown(KeyEvent.KEYCODE_BACK, null);
    }

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0,android.R.anim.slide_out_right);
    }

    private boolean isPermissionGranted() {
        try{
            String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
            int result = ContextCompat.checkSelfPermission(this, recordAudioPermission);
            return result == PackageManager.PERMISSION_GRANTED;
        }catch(Exception e){
            System.out.println("Error Occured : Checking Mic Permissions, Scope : isPermissionGranted,  Error : " + e.getMessage());
            return false;
        }
    }

    private void requestPermission() {
        try{
            String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
            ActivityCompat.requestPermissions(this, new String[]{recordAudioPermission}, RECORD_AUDIO_PERMISSION_REQUEST);
        }catch (Exception e){
            System.out.println("Error Occured : Requesting Mic Permissions, Scope : requestPermission,  Error : " + e.getMessage());
        }
    }
    private void openAppSettings() {
        try{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 123);
        }catch(Exception e){
            System.out.println("Error Occured : Requesting Mic Permissions, Scope : openAppSettings,  Error : " + e.getMessage());
        }
    }

    private void showPermissionSettingsDialog() {
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Microphone Permission Required");
            builder.setMessage("To use this feature, you need to enable the microphone permission in the app settings.");
            builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Open the app settings to allow the user to enable the permission.
                    openAppSettings();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle the user's decision to cancel.
                }
            });
            builder.show();
        }catch(Exception e){
            System.out.println("Error Occured : Requesting Mic Permissions, Scope : showPermissionSettingsDialog,  Error : " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try{
            if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can proceed with audio recording
                    // Implement your audio recording logic here
                } else {
                    if(!isPermissionGranted()){
                        showPermissionSettingsDialog();
                    }
                }
            }
        }catch(Exception e){
            System.out.println("Error Occured : Requesting Mic Permissions, Scope : onRequestPermissionsResult,  Error : " + e.getMessage());
        }
    }


    public class OriJavaScriptInterface {
        OriJavaScriptInterface() {

        }

        @JavascriptInterface
        public void textFromWeb(String obj) {
            OriLog(TAG, "Got string from JS Interface: " + obj);

            try {
                JSONObject json = new JSONObject(obj);
                if (json.has("button")) {
                    Log.d("myTag", obj);
                        JSONObject button = json.getJSONObject("button");
                    if (button.has("deeplink") && !(button.getString("deeplink").equalsIgnoreCase(""))) {
                        OriLog(TAG, "Button has deeplink: and it is not empty");
                        String type = button.getString("deeplink");
                        OriChatBot mySDK = OriChatBot.getInstance();
                        JSONObject data = new JSONObject();
                        data.put("case", "handleRedirection");
                        data.put("type", "deeplink");
                        data.put("value", type);
                        mySDK.fireEvent(data);
                        finish();


//                        Uri location = Uri.parse(type);
//                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
//                        mapIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(type));
//                        startActivity(mapIntent);
                    }
                    else if (button.has("b_id") && !(button.getString("b_id").equalsIgnoreCase(""))) {
                        // has b_id and it is not empty
//                        Log.d("myTag", "BID found");
                        OriLog(TAG, "Button has b_id: and it is not empty");
                        String type = button.getString("b_id");

                        //if (json.has("data")) {
//                            Intent intent = new Intent();
////                            intent.setData(Uri.parse("myvi://myvi.in/recommended_offers"));
////                            startActivity(intent);
//                            intent.putExtra("type", type);
//                            intent.putExtra("data", json.getJSONObject("data").toString());
//                            Log.d(TAG, "data found: ");
//                            setResult(RESULT_OK, intent);
//                            finish();
                            Uri location = Uri.parse(type);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
                            mapIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(type));
                            startActivity(mapIntent);
//                            finish();
                      //  }

                    } else if (button.has("androidType") && !(button.getString("androidType").equalsIgnoreCase(""))
                            && button.has("androidTypeKey")
                            && !(button.getString("androidTypeKey").equalsIgnoreCase(""))) {
                        // has b_id and it is not empty
                        OriLog(TAG, "Button has androidType: and it is not empty");
                        String type = button.getString("androidTypeKey");
                        String typeKey = button.getString("androidType");
                        OriLog(TAG, type);
                        OriLog(TAG, typeKey);
                        /*
                         * if (json.has("data")) { Intent intent = new Intent(); intent.putExtra("type",
                         * type); intent.putExtra("data",json.getJSONObject("data").toString());
                         * setResult(RESULT_OK, intent); finish(); }
                         */

                        Intent intent = new Intent();
                        intent.putExtra("type", type);
                        intent.putExtra("data", typeKey);
                        setResult(RESULT_OK, intent);
                        finish();

                    } else { // did not receive b_id
                        openPage(button.getString("url"));
                    }
                } else {
                    OriLog(TAG, "NOOOOOOOOO button -------------");
                }
            } catch (Exception e) {
                OriLog(TAG, "Got error ooooo from JS Interface: ");
                e.printStackTrace();
            }

        }

        @JavascriptInterface
        public void updateFromReact(String type, String data) {
            OriLog(TAG, "UpdateFromWeb called: " + type + " " + data);
            try {
                if (type.equals("endChatSubmit")) {
                    OriLog(TAG, "endChatSubmit success: ");
                    // webViewClose
                    finish();
                    endChat();
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                        webView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                webView.evaluateJavascript("localStorage.setItem('"+ "appMinimizedfrom" +"','"+ "" +"');", null);
//                                webView.evaluateJavascript("localStorage.setItem('"+ "chatBotTimeStamp" +"','"+ "" +"');", null);
//                            }
//                        });
//                    } else {
//                        webView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                webView.loadUrl("javascript:localStorage.setItem('"+ "appMinimizedfrom" +"','"+ "" +"');");
//                                webView.loadUrl("javascript:localStorage.setItem('"+ "chatBotTimeStamp" +"','"+ "" +"');");
//                            }
//                        });
//
//                    }
                }
            } catch (Exception e) {
                OriLog(TAG, "UpdateFromWeb error: ");
                e.printStackTrace();
            }
        }
    }

    private void endChat(){
        try{
            String key = "appMinimizedFrom";
            String val = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.evaluateJavascript("localStorage.setItem('"+ "appMinimizedfrom" +"','"+ "" +"');", null);
                                webView.evaluateJavascript("localStorage.setItem('"+ "chatBotTimeStamp" +"','"+ "" +"');", null);
                            }
                        });
            } else {
                webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:localStorage.setItem('"+ "appMinimizedfrom" +"','"+ "" +"');");
                                webView.loadUrl("javascript:localStorage.setItem('"+ "chatBotTimeStamp" +"','"+ "" +"');");
                            }
                        });
            }
        } catch (Exception e)
        {
            OriLog(TAG, "okat okay error: ");
            e.printStackTrace();
        }
    }

    private void openPage(String url) {
        if (!url.equalsIgnoreCase("")) {
            if(isInternalRedirection){
                Intent i = new Intent(this, WebActivity.class);
                i.putExtra("showToolbar", getIntent().getBooleanExtra("showToolbar", true));
                i.putExtra("showMiniToolbar", getIntent().getBooleanExtra("showMiniToolbar", false));
                i.putExtra("pageTitle", "");
                i.putExtra("pageURL", url);
                startActivity(i);
            }else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }
    }

    private static boolean isJsonString(String input) {
        try {
            // Try to parse the string as a JSON object
            new JSONObject(input);
            return true;
        } catch (JSONException e1) {
            try {
                // If it's not a JSON object, try parsing as a JSON array
                new JSONArray(input);
                return true;
            } catch (JSONException e2) {
                return false;
            }
        }
    }





private void injectBackButtonAction() {
        OriLog(TAG, "callingBack Button JS function");
        JSONObject backPressedObj = new JSONObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // evaluateJS is not available in lower android
            webView.evaluateJavascript("window.androidObj.updateFromAndroid(\'endchat\',\'" + backPressedObj.toString() + "\');",
                    null);
        } else {
            webView.loadUrl("javascript: window.androidObj.updateFromAndroid(\'endchat\',\'" + backPressedObj.toString() + "\');");
        }
    }

    private void injectJavaScriptFunction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // evaluateJS is not available in lower android
            // versions
            webView.evaluateJavascript("window.androidObj.textToAndroid = function(message) { " + JAVASCRIPT_OBJ
                    + ".textFromWeb(message) }", null);
            webView.evaluateJavascript("window.androidObj.updateFromWeb = function(type,data) { " + JAVASCRIPT_OBJ
                    + ".updateFromReact(type,data) }", null);
        } else {
            webView.loadUrl("javascript: " + "window.androidObj.textToAndroid = function(message) { " + JAVASCRIPT_OBJ
                    + ".textFromWeb(message) }");
            webView.loadUrl("javascript: " + "window.androidObj.updateFromWeb = function(type,data) { " + JAVASCRIPT_OBJ
                    + ".updateFromReact(type,data) }");
        }
        OriLog(TAG, "calling JS function");

        JSONObject obj = new JSONObject();
        try {

//            String vcNumber;
//            String rmn;
            String msisdn;
            try {
                // vcNumber = getIntent().getStringExtra("vcNumber");
                msisdn = getIntent().getStringExtra("msisdn");
            } catch (Exception e) {
//                vcNumber = "";
                msisdn = "";
            }
            String stateCode;
            try {
                // vcNumber = getIntent().getStringExtra("vcNumber");
                stateCode = getIntent().getStringExtra("statecode");
            } catch (Exception e) {
//                vcNumber = "";
                stateCode = "";
            }
            String customerName;
            try {
                // vcNumber = getIntent().getStringExtra("vcNumber");
                customerName = getIntent().getStringExtra("customername");
            } catch (Exception e) {
//                vcNumber = "";
                customerName = "";
            }
            String brand;
            try {
                // vcNumber = getIntent().getStringExtra("vcNumber");
                brand = getIntent().getStringExtra("brand");
            } catch (Exception e) {
//                vcNumber = "";
                brand = "";
            }
            String lob;
            try {
                // vcNumber = getIntent().getStringExtra("vcNumber");
                lob = getIntent().getStringExtra("lob");
            } catch (Exception e) {
//                vcNumber = "";
                lob = "";
            }
            String customerGroup;
            try {
                // vcNumber = getIntent().getStringExtra("vcNumber");
                customerGroup =  getIntent().getStringExtra("brand")+" "+ getIntent().getStringExtra("lob") ;
            } catch (Exception e) {
//                vcNumber = "";
                customerGroup = "";
            }
            String journeyData;
            try{
                journeyData =  getIntent().getStringExtra("journeyData");
            } catch (Exception e) {
                journeyData = "";
            }

            OriLog(journeyData, "testing purpose");

//            try {
//                rmn = getIntent().getStringExtra("rmn");
//            } catch (Exception e) {
//                rmn = "";
//            }
            prefsKey = msisdn;
//            if (vcNumber.equalsIgnoreCase("")) { // vc field is empty
//                prefsKey = rmn;
//            } else {
//                prefsKey = vcNumber;
//            }
            String appDetails = "";
            try {
                appDetails = getIntent().getStringExtra("appdetails");
            } catch (Exception e) {
                Log.d("JSONOBJ", "NOT FOUND");
                appDetails = "";
            }

            JSONObject jsonobj = new JSONObject();
            try {
                Log.d("THIS IS JD", journeyData);
                jsonobj.put("appDetails", appDetails);
                jsonobj.put("brand", brand);
                jsonobj.put("stateCode", stateCode);
                jsonobj.put("customerName", customerName);
                jsonobj.put("lob", lob);
                jsonobj.put("customerGroup", customerGroup);
//                jsonobj.put("journeyData", journeyData);
            } catch (Exception e) {
                Log.d("JSONOBJ", "NOT FOUND");
            }

            JSONObject JData = new JSONObject();
            if(journeyData != "") {
                try {
                    JData = new JSONObject(journeyData);
                }catch (Exception e) {
                    Log.d("JSONOBJ", "NOT FOUND");
                }
            }

//          getPSID2();
            obj.put("psid", getPSID());
            obj.put("psid", psid);
            JSONObject rawData = new JSONObject();
            rawData.put("udf1", msisdn);
//            rawData.put("udf2", rmn);
            rawData.put("udf2", "");
            rawData.put("udf3", JData);
            rawData.put("udf4", "");
            rawData.put("udf5", "");
            rawData.put("brandName", OriConstants.brandName);
            rawData.put("brand", brand);
            rawData.put("stateCode", stateCode);
            rawData.put("customerName", customerName);
            rawData.put("lob", lob);
            rawData.put("customerGroup", customerGroup);
            rawData.put("la", "true");
            if (appDetails == "") {
                rawData.put("appdetails", appDetails);
            }
            else
            {
                rawData.put("appdetails", jsonobj);
            }
            JSONObject lockedParams = new JSONObject();


            /*
                userData as new Key for all parameters
            */
            String userData;
            try {
                userData = getIntent().getStringExtra("userData");
            } catch (Exception e) {
                userData = "";
            }
            if(!userData.isEmpty()){
                // Convert the JSON string to a JSONObject
                rawData = JsonUtils.convertJsonStringToJsonObjectWithAllLevels(userData);
            }


            lockedParams.put("rawData", rawData);

            rawData.put("token", OriConstants.token2);

            lockedParams.put("hash", returnSHA(rawData.toString(), OriConstants.token));

            obj.put("params", lockedParams);

            OriLog(TAG, "params : " + rawData.toString());
            OriLog(TAG, "encrypted params : " + returnSHA(rawData.toString(), OriConstants.token));
//            String jsonString = obj.toString();
//            OriLog(TAG, "obj to fe : " + jsonString.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // evaluateJS is not available in lower android
            // versions
//            OriLog(TAG, "obj to fe1 : " + obj.toString());
            webView.evaluateJavascript("window.androidObj.updateFromAndroid(\'android\',\'\');", null);
            webView.evaluateJavascript("window.androidObj.updateFromAndroid(\'psid\',\'" + obj.toString() + "\');",
                    null);
        } else {
            webView.loadUrl("javascript: window.androidObj.updateFromAndroid(\'android\',\'\');");
            webView.loadUrl("javascript: window.androidObj.updateFromAndroid(\'psid\',\'" + obj.toString() + "\');");
        }
    }

    private static void OriLog(String TAG, String msg) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, msg);
    }

    @SuppressLint("ApplySharedPref")
    void savePSID(String id) {
        SharedPreferences prefs = getSharedPreferences("ORI_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefsKey, id);
        editor.commit();
//        Log.d("Saved Psid",id);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            webView.evaluateJavascript("localStorage.setItem('"+ prefsKey +"','"+ id +"');", null);
//        } else {
//            webView.loadUrl("javascript:localStorage.setItem('"+ prefsKey +"','"+ id +"');");
//        }

    }

    String getPSID() {
        SharedPreferences prefs = getSharedPreferences("ORI_PREFS", Context.MODE_PRIVATE);
        OriLog(TAG, "Shared Preferences " + prefs);

        // prefs are stored with either vc or rmn
        if (!prefsKey.equalsIgnoreCase("")) {
            String storedPsid = prefs.getString(prefsKey, "");
            if (storedPsid.equalsIgnoreCase("")) {
                if(psid.equalsIgnoreCase("")) {
                    psid = guid();
                    OriLog(TAG, "Generated new PSID: " + psid);
                    savePSID(psid);
                    return psid;
                }
            }
            OriLog(TAG, "Retrieved old PSID: " + storedPsid);
            return storedPsid;
        }

        return guid();
    }

    String getPSIDfromSharedPrefs() {
        SharedPreferences prefs = getSharedPreferences("ORI_PREFS", Context.MODE_PRIVATE);
        OriLog(TAG, "Shared Preferences " + prefs);
        // prefs are stored with either vc or rmn
        if (!prefsKey.equalsIgnoreCase("")) {
            String storedPsid = prefs.getString(prefsKey, "");
            if (storedPsid.equalsIgnoreCase("")) {
                if(psid.equalsIgnoreCase("")) {
                    psid = guid();
                    OriLog(TAG, "Generated new PSID: " + psid);
                    savePSID(psid);
                    return psid;
                }
            }
            else
            {
                OriLog(TAG, "Retrieved old PSID: " + storedPsid);
                return storedPsid;
            }

        }
        return "";
    }

    String getPSID2(){
        psid = getPSIDfromSharedPrefs();
        if(psid.equalsIgnoreCase(""))
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript("localStorage.getItem('" + prefsKey + "');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        if(!s.equalsIgnoreCase("")) {
                            Log.d("generate psid LogName", s);
                            psid = s;
                            Log.d("generate LogName Psid2", psid);
                        }
                    }
                });
            }
        }
        else
        {
            return psid;
        }
        psid = guid();
        savePSID(psid);
        return psid;

    }
    static String s4() {
        int d = (int) (Math.floor((1 + Math.random()) * 0x10000));
        return Integer.toString(d).substring(1);

    }

    private static String guid() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        return "and-" + s4() + "-" + s4() + "-" + s4() + "-" + timeStamp;
    }

    /**
     * This method checks if mobile is connected to network.
     *
     * @param context of current activity
     * @return true if connected otherwise false.
     */
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (conMan != null && conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected());
    }

    /**
     * @param value string to be encrypted
     * @return hash value which can then be passed to server
     */
    public static String returnSHA(String value, String key) {
        String retval = "";
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
            mac.init(secret);
            retval = Base64.encodeToString(mac.doFinal(value.getBytes("UTF-8")), Base64.NO_WRAP);
            // OriUtil.OriLog("confirmretval : ", retval);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return retval;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpUI() {
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("showToolbar", true)) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getIntent().getStringExtra("pageTitle"));
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    ViewGroup holder = findViewById(R.id.toolbar_holder);
                    holder.setVisibility(View.VISIBLE);
                    getLayoutInflater().inflate(R.layout.ori_toolbar, holder);
                    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(getIntent().getStringExtra("pageTitle"));
                        getSupportActionBar().setHomeButtonEnabled(true);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                }
            } else {
                // findViewById(R.id.toolbar_holder).setVisibility(View.GONE);
                if (getSupportActionBar() != null) { // remove appbar if exists
                    getSupportActionBar().hide();
                }
            }

            if (getIntent().getBooleanExtra("showMiniToolbar", false)) {

                LinearLayout linearLayout = findViewById(R.id.header);
                linearLayout.setVisibility(View.VISIBLE);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            } else {
                findViewById(R.id.header).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

}
