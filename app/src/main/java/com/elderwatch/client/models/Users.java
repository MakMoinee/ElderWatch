package com.elderwatch.client.models;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Users {
    private String userID;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private int userType;
    private String registeredDate;


}
