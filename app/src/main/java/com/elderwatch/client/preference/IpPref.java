package com.elderwatch.client.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class IpPref {
    Context mContext;
    SharedPreferences pref;

    public IpPref(Context mContext) {
        this.mContext = mContext;
        this.pref = mContext.getSharedPreferences("ip", Context.MODE_PRIVATE);
    }

    public void storeIP(String ip) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("ip", ip);
        editor.commit();
        editor.apply();
    }

    public String getIP() {
        return this.pref.getString("ip", "");
    }

    public void clear(){
        this.pref.getAll().clear();
    }
}
