package com.elderwatch.client.models;

import lombok.Data;

@Data
public class ActivityHistory
{
    private String activityHistoryID;
    private String caregiverID;
    private String imagePath;
    private String createdAt;
    private String status;

    public static class ActivityHistoryBuilder{
        private String activityHistoryID;
        private String caregiverID;
        private String imagePath;
        private String createdAt;
        private String status;

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
    }
}
