package com.oriserve.orichat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OriChatBot {
//    public static final int ORI_MIN_SDK = 16;
    public static final int ORI_MIN_SDK = Build.VERSION_CODES.KITKAT;
    public static final int ORI_CODE = 13;
    private boolean showMiniToolbar = false;
    private boolean showToolbar = true;
    private String pageTitle = "";
    private String VC = "";
    private String MSISDN = "";
    private String brand = "";
    private String stateCode = "";
    private String lob = "";
    private String customerName = "";
    private String customerGroup = "";
    private String RMN = "";
    private String url = "";
    private String AppDetails;
    private String journeyData = "";
    private String baseUrl = "";
    private String userData = "";

    private String botIdentifier = "";

    private Activity mContext;

    private static OriChatBot instance;
    private static List<EventListener> eventListeners = new ArrayList<>();

    public static OriChatBot getInstance() {
        if (instance == null) {
            instance = new OriChatBot(null);
        }
        return instance;
    }

    public interface EventListener {
        void onEventReceived(JSONObject data);
    }

    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public void fireEvent(JSONObject data) {
        for (EventListener listener : eventListeners) {
            listener.onEventReceived(data);
            break;
        }
        eventListeners.clear();
    }


    public OriChatBot(Activity activity) {
        mContext = activity;
    }

    public static boolean isAvailable() {
        return Build.VERSION.SDK_INT >= ORI_MIN_SDK;
    }

    /**
     * Launches the Bot activity over the context passed. Use onActivityForResult to get the result
     */
    public void launchBot(){

        if (Build.VERSION.SDK_INT >= ORI_MIN_SDK) {
            Intent i = new Intent(mContext, OriChatActivity.class);
            i.putExtra("showToolbar", showToolbar);
            i.putExtra("showMiniToolbar", showMiniToolbar);
            i.putExtra("pageTitle", pageTitle);
//            i.putExtra("vcNumber", VC);
            i.putExtra("msisdn", MSISDN);
            i.putExtra("brand", brand);
            i.putExtra("statecode", stateCode);
            i.putExtra("customername", customerName);
            i.putExtra("customerGroup", customerGroup);
            i.putExtra("userData", userData);
            i.putExtra("baseUrl", baseUrl);
            i.putExtra("botIdentifier", botIdentifier);
            i.putExtra("lob", lob);
//            i.putExtra("rmn", RMN);
            i.putExtra("pageURL", url);
            i.putExtra("appdetails", AppDetails);
            i.putExtra("journeyData", journeyData);

            if (mContext != null)
                mContext.startActivityForResult(i, ORI_CODE);
        } else {
            if (mContext != null)
                showToast(mContext,"Chatbot is not available for this version of android");
        }
    }

    private static Toast toast;

    /**
     * universal function to toast messages. It will override and previous message shown by app
     *
     * @param context required to add toast message to system service
     * @param msg     the message to be shown in toast
     */
    public static void showToast(Context context, String msg) {
        if (msg == null || context == null)
            return;
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public OriChatBot showToolbar(boolean flag) {
        this.showToolbar = flag;

        if (flag) { //hide the mini toolbar in this case
            this.showMiniToolbar = false;
        }
        return this;
    }

    public OriChatBot setBotIdentifier(String botIdentifier) {
        this.botIdentifier = botIdentifier;
        return this;
    }

    public OriChatBot setBotId(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public OriChatBot setUserData(String userData) {
        this.userData = userData;
        return this;
    }



    public OriChatBot showMiniToolbar(boolean flag) {
        this.showMiniToolbar = flag;

        if (flag) { //hide the main toolbar in this case
            this.showToolbar = false;
        }
        return this;
    }

    public OriChatBot setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    public OriChatBot setVC(String VC) {
        this.VC = VC;
        return this;
    }

    public OriChatBot setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
        return this;
    }
    public OriChatBot setBrand(String brand) {
        this.brand = brand;
        return this;
    }
    public OriChatBot setStateCode(String stateCode) {
        this.stateCode = stateCode;
        return this;
    }
    public OriChatBot setCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }
    public OriChatBot setLob(String lob) {
        this.lob = lob;
        return this;
    }
    public OriChatBot setCustomerGroup(String brand,String lob) {
        this.customerGroup = brand + lob;
        return this;
    }
    public OriChatBot setAppDetails(String AppDetails) {
        this.AppDetails = AppDetails;
        return this;
    }
    public OriChatBot setJourneyData(String journeyData) {
        Log.d("This is journey data",journeyData);
        this.journeyData = journeyData;
        return this;
    }


    public OriChatBot setRMN(String RMN) {
        this.RMN = RMN;
        return this;
    }
}
