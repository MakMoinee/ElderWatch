package com.elderwatch.client.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceTokenPref {
    Context mContext;
    SharedPreferences pref;

    public DeviceTokenPref(Context mContext) {
        this.mContext = mContext;
        this.pref = mContext.getSharedPreferences("deviceTokens", Context.MODE_PRIVATE);
    }

    public void storeToken(String token) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("deviceToken", token);
        editor.commit();
        editor.apply();
    }

    public String getToken() {
        return pref.getString("deviceToken", "");
    }
}
