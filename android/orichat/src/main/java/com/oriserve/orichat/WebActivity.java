package com.oriserve.orichat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.net.URISyntaxException;

public class WebActivity extends AppCompatActivity {

    static final String TAG = "WebActivityTAG";
    private WebView webView;
    private String baseUrl;
    private boolean webviewSuccess = true;
    long lastBackPressed = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ori_chat);

        setUpUI();

        final ProgressBar progressBar = findViewById(R.id.loading_indicator);
        final View noInternet = findViewById(R.id.no_internet);
//        overridePendingTransition(0,android.R.anim.slide_out_right);
        webView = findViewById(R.id.webView);

        if (getIntent().hasExtra("pageURL")) {
            String url = getIntent().getStringExtra("pageURL");
            if (!url.equalsIgnoreCase(""))
                baseUrl = url;
            else baseUrl = "";
        } else {
            baseUrl = "";
        }
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);



        webView.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                OriLog(TAG,"Got string from JS: "+url);
//                OriLog(TAG,"Upar waala"+url);
//                view.loadUrl(baseUrl);
                return false;
            }

            @TargetApi(Build.VERSION_CODES.N)
            //for api 25 and above, this method was introduced and prev one deprecated
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                OriLog(TAG, "Got string from JS: " + request.getUrl().toString());

                if (request.getUrl().toString().startsWith("intent://")){
                    try {

                        Context context = view.getContext();
                        Intent intent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                        if(intent!= null)
                        {
                            PackageManager packageManager = context.getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            Log.d("GONE IN INTENT", "GONE IN INTENT");
                            if(info!=null)
                            {
                                Log.d("assadf", "INFO IS NOT NULL");
                                context.startActivity(intent);
                            }else {
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                OriLog("This is failing URL",fallbackUrl);
                            }
                            return true;
                        }
                    }catch(URISyntaxException e){
                        OriLog("Can't resolve intent:// ","error");
                    }

                }
                view.loadUrl(request.getUrl().toString());
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(webviewSuccess) {
                    OriLog(TAG, "reached here: Page loaded for url " + url +" base url: "+baseUrl);
                    try {
                        progressBar.setVisibility(View.GONE);
                        noInternet.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                webviewSuccess = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    OriLog(TAG, "Got an error " + error.getDescription().toString());
                } else {
                    OriLog(TAG, "Got an error " + error.toString());
                }

                try {
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    noInternet.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                super.onReceivedError(view, request, error);
            }
        });

        webView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
//                OriLog("CONSOLE_LOG", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
                OriLog(TAG+"_CONSOLE", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
                return true;
            }
        });

        ImageButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(WebActivity.this)) {
                    progressBar.setVisibility(View.VISIBLE);
                    noInternet.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);

                    webviewSuccess = true;
                    webView.loadUrl(baseUrl);
                }
            }
        });

        if (isNetworkAvailable(this)) {
            progressBar.setVisibility(View.VISIBLE);
            noInternet.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);

            webviewSuccess = true;
            webView.loadUrl(baseUrl);
        } else {
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
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
//                findViewById(R.id.toolbar_holder).setVisibility(View.GONE);
                if (getSupportActionBar() != null) { //remove appbar if exists
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

    private static void OriLog(String TAG, String msg) {
        if(BuildConfig.DEBUG)
            Log.d(TAG,msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyWebView();
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

    public void destroyWebView() {

        // Make sure you remove the WebView from its parent view before doing anything.
        webView.setVisibility(View.GONE);

        webView.clearHistory();

        webView.onPause();
        webView.removeAllViews();
        webView.destroyDrawingCache();

        // Null out the reference so that you don't end up re-using it.
        webView = null;
    }

}
