package com.elderwatch.client.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CaregiverActivity {
    private String activityID;
    private String date;
    private String time;
    private String caregiverID;
    private String patientID;

    public CaregiverActivity(CaregiverActivityBuilder builder) {
        this.activityID = builder.activityID;
        this.date = builder.date;
        this.time = builder.time;
        this.caregiverID = builder.caregiverID;
        this.patientID = builder.patientID;
    }

    public static class CaregiverActivityBuilder {
        private String activityID;
        private String date;
        private String time;
        private String caregiverID;
        private String patientID;


        public CaregiverActivityBuilder setActivityID(String activityID) {
            this.activityID = activityID;
            return this;
        }

        public CaregiverActivityBuilder setDate(String date) {
            this.date = date;
            return this;
        }

        public CaregiverActivityBuilder setTime(String time) {
            this.time = time;
            return this;
        }

        public CaregiverActivityBuilder setCaregiverID(String caregiverID) {
            this.caregiverID = caregiverID;
            return this;
        }

        public CaregiverActivityBuilder setPatientID(String patientID) {
            this.patientID = patientID;
            return this;
        }

        public CaregiverActivity build() {
            return new CaregiverActivity(this);
        }
    }
}
