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

    public String getEmail() {
        String email = "";

        for (Map.Entry<String, Object> obj : this.getLogin().entrySet()) {
            if (obj.getKey().equals("email")) {
                email = obj.getValue().toString();
                break;
            }
        }

        return email;
    }

    public String getFullName() {
        String firstName = "";
        String middleName = "";
        String lastName = "";

        for (Map.Entry<String, Object> obj : this.getLogin().entrySet()) {
            if (obj.getKey().equals("firstName")) {
                firstName = obj.getValue().toString();
            }
            if (obj.getKey().equals("middleName")) {
                middleName = obj.getValue().toString();
            }
            if (obj.getKey().equals("lastName")) {
                lastName = obj.getValue().toString();
            }
        }

        return String.format("%s, %s %s", lastName, firstName, middleName);
    }
}
