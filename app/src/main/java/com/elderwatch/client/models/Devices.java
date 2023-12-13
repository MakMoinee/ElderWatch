package com.elderwatch.client.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Devices {
    private String deviceID;
    private String userID;
    private String ip;
    private String username;
    private String password;
}
