package com.elderwatch.client.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PatientGuardian {

    private String patientID;
    private String userID;

    private String caregiverID;
    private String deviceID;
    private String ip;
    private String phoneNumber;

    public PatientGuardian(PatientGuardianBuilder builder) {
        this.patientID = builder.patientID;
        this.userID = builder.userID;
        this.caregiverID = builder.caregiverID;
        this.deviceID = builder.deviceID;
        this.ip = builder.ip;
        this.phoneNumber = builder.phoneNumber;
    }

    public static class PatientGuardianBuilder {
        private String patientID;
        private String userID;

        private String caregiverID;
        private String deviceID;
        private String ip;
        private String phoneNumber;

        public PatientGuardianBuilder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public PatientGuardianBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public PatientGuardianBuilder setDeviceID(String deviceID) {
            this.deviceID = deviceID;
            return this;
        }

        public PatientGuardianBuilder setCaregiverID(String caregiverID) {
            this.caregiverID = caregiverID;
            return this;
        }

        public PatientGuardianBuilder setPatientID(String patientID) {
            this.patientID = patientID;
            return this;
        }

        public PatientGuardianBuilder setUserID(String userID) {
            this.userID = userID;
            return this;
        }

        public PatientGuardian build() {
            return new PatientGuardian(this);
        }
    }
}
