package com.oriserve.orichat;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


/**
   * JsonUtils is a Java class that facilitates the conversion of a JSONString into a JSONObject, encompassing all layers of nested objects.
   * In contrast, standard Java conversion only handles the first layer of the JSONString, not the nested ones.
   * @param JSON stirng
   * @Return JSONObject
 */
public class JsonUtils {

    public static JSONObject convertJsonStringToJsonObjectWithAllLevels(String jsonString) {
        try {
            return convertJsonObject((String) jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject convertJsonObject(String jsonObject) throws JSONException {
        JSONObject newJsonObject = new JSONObject();
        JSONObject rawData = new JSONObject(jsonObject);
        for (Iterator<String> it = rawData.keys(); it.hasNext();) {
            String key = it.next();
            Object value = rawData.opt(key);
            Object returnedValue = convertJsonElement(value.toString());
            if(returnedValue.equals("true") || returnedValue.equals("false")){
                newJsonObject.put(key, Boolean.getBoolean((String) returnedValue));
                continue;
            }
            newJsonObject.put(key, returnedValue);
        }
        return newJsonObject;
    }

    private static JSONArray convertJsonArray(String stringArray) throws JSONException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new JSONArray(stringArray);
        }
        JSONArray array = new JSONArray();
        array.put(stringArray);
        return array;
    }

    private static Object convertJsonElement(String element) throws JSONException {
        if (isJsonStringorArray((String) element) == "object") {
            return convertJsonObject((String) element);
        } else if (isJsonStringorArray((String) element) == "array") {
            return convertJsonArray((String) element);
        } else if(isJsonStringorArray((String) element) == "boolean"){
            JSONObject isBoolean = new JSONObject();
            isBoolean.put("isBooloean", element);
            return isBoolean;
        }{
            return element;
        }
    }

    private static String isJsonStringorArray(String input) throws JSONException {
        if(Boolean.parseBoolean(input)){
            return "boolean";
        }
        try {
            // Try to parse the string as a JSON object
            new JSONObject(input);
            return "object";
        } catch (JSONException e1) {
            try {
                // If it's not a JSON object, try parsing as a JSON array
                new JSONArray(input);
                return "array";
            } catch (JSONException e2) {
                return "";
            }
        }
    }
}
