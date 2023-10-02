package com.elderwatch.client.preference;

import android.content.Context;

import com.github.MakMoinee.library.preference.LoginPref;

import java.util.Map;

public class UserPref extends LoginPref {
    public UserPref(Context mContext) {
        super(mContext);
    }

    public String getUserID() {
        String userID = "";

        for (Map.Entry<String, Object> obj : this.getLogin().entrySet()) {
            if (obj.getKey().equals("userID")) {
                userID = obj.getValue().toString();
                break;
            }
        }

        return userID;
    }
}
