package com.elderwatch.client.preference;

import android.content.Context;

import com.elderwatch.client.models.Users;
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

    public Users getUsers() {
        Users users = new Users();

        for (Map.Entry<String, Object> obj : this.getLogin().entrySet()) {
            switch (obj.getKey()) {
                case "userID" -> {
                    users.setUserID((String) obj.getValue());
                }
                case "firstName" -> {
                    users.setFirstName((String) obj.getValue());
                }
                case "middleName" -> {
                    users.setMiddleName((String) obj.getValue());
                }
                case "lastName" -> {
                    users.setLastName((String) obj.getValue());
                }
                case "email" -> {
                    users.setEmail((String) obj.getValue());
                }
                case "password" -> {
                    users.setPassword((String) obj.getValue());
                }
                case "address" -> {
                    users.setAddress((String) obj.getValue());
                }
                case "birthDate" -> {
                    users.setBirthDate((String) obj.getValue());
                }
                case "phoneNumber" -> {
                    users.setPhoneNumber((String) obj.getValue());
                }
                case "userType" -> {
                    users.setUserType((int) obj.getValue());
                }
                case "registeredDate" -> {
                    users.setRegisteredDate((String) obj.getValue());
                }
            }
        }

        return users;
    }
}
