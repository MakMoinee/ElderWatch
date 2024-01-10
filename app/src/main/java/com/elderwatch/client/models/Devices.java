package com.elderwatch.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Devices {
    private String deviceID;
    private String userID;
    private String ip;
    private String username;
    private String password;
    private String status;

}
