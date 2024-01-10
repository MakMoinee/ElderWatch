package com.elderwatch.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParentCustomToken {
    private String userIDMap;
    private String docID;
    private String userID;
    private String deviceToken;

    private String ip;

    public ParentCustomToken(ParentCustomTokenBuilder builder){
        this.userIDMap = builder.userIDMap;
        this.docID = builder.docID;
        this.userID = builder.userID;
        this.deviceToken = builder.deviceToken;
    }

    public static class ParentCustomTokenBuilder {
        private String userIDMap;
        private String docID;
        private String userID;
        private String deviceToken;

        private String ip;

        public ParentCustomTokenBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public ParentCustomTokenBuilder setUserIDMap(String userIDMap) {
            this.userIDMap = userIDMap;
            return this;
        }

        public ParentCustomTokenBuilder setDocID(String docID) {
            this.docID = docID;
            return this;
        }

        public ParentCustomTokenBuilder setUserID(String userID) {
            this.userID = userID;
            return this;
        }

        public ParentCustomTokenBuilder setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
            return this;
        }

        public ParentCustomToken build(){
            return new ParentCustomToken(this);
        }
    }
}
