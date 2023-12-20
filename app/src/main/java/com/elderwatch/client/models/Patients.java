package com.elderwatch.client.models;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Patients {
    private String patientID;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String address;
    private String birthDate;

    public Patients(PatientBuilder builder) {
        this.patientID = builder.patientID;
        this.firstName = builder.firstName;
        this.middleName = builder.middleName;
        this.lastName = builder.lastName;
        this.address = builder.address;
        this.birthDate = builder.birthDate;
        this.fullName = builder.fullName;
    }

    public static class PatientBuilder {
        private String patientID;
        private String firstName;
        private String middleName;
        private String lastName;
        private String address;
        private String birthDate;

        private String fullName;

        public PatientBuilder setFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public PatientBuilder setPatientID(String patientID) {
            this.patientID = patientID;
            return this;
        }

        public PatientBuilder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public PatientBuilder setMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public PatientBuilder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public PatientBuilder setAddress(String address) {
            this.address = address;
            return this;
        }

        public PatientBuilder setBirthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Patients build() {
            return new Patients(this);
        }
    }
}
