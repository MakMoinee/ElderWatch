package com.elderwatch.client.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ActivityHistory
{
    private String activityHistoryID;
    private String caregiverID;
    private String imagePath;
    private String createdAt;
    private String status;
    private String ip;
    private String remarks;

    public ActivityHistory(ActivityHistoryBuilder builder){
        this.activityHistoryID = builder.activityHistoryID;
        this.caregiverID = builder.caregiverID;
        this.imagePath = builder.imagePath;
        this.createdAt = builder.createdAt;
        this.status = builder.status;
        this.ip = builder.ip;
        this.remarks = builder.remarks;
    }

    public static class ActivityHistoryBuilder{
        private String activityHistoryID;
        private String caregiverID;
        private String imagePath;
        private String createdAt;
        private String status;

        private String ip;
        private String remarks;

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public ActivityHistoryBuilder setActivityHistoryID(String activityHistoryID) {
            this.activityHistoryID = activityHistoryID;
            return this;
        }

        public ActivityHistoryBuilder setCaregiverID(String caregiverID) {
            this.caregiverID = caregiverID;
            return this;
        }

        public ActivityHistoryBuilder setImagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public ActivityHistoryBuilder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ActivityHistoryBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        public ActivityHistoryBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public ActivityHistory build(){
            return new ActivityHistory(this);
        }
    }
}
