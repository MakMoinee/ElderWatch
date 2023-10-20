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
    private String middleName;
    private String lastName;
    private String address;
    private String birthDate;
    private String phoneNumber;
    private int userType;
    private String registeredDate;

    public Users(UserBuilder builder) {
        this.userID = builder.userID;
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.middleName = builder.middleName;
        this.lastName = builder.lastName;
        this.address = builder.address;
        this.birthDate = builder.birthDate;
        this.phoneNumber = builder.phoneNumber;
        this.userType = builder.userType;
        this.registeredDate = builder.registeredDate;
    }

    public static class UserBuilder {
        private String userID;
        private String email;
        private String password;
        private String firstName;

        private String middleName;
        private String lastName;

        private String address;
        private String birthDate;
        private String phoneNumber;

        private int userType;
        private String registeredDate;

        public UserBuilder() {

        }

        public UserBuilder setUserID(String userID) {
            this.userID = userID;
            return this;
        }

        public UserBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder setMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public UserBuilder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder setUserType(int userType) {
            this.userType = userType;
            return this;
        }

        public UserBuilder setAddress(String address) {
            this.address = address;
            return this;
        }

        public UserBuilder setBirthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public UserBuilder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder setRegisteredDate(String registeredDate) {
            this.registeredDate = registeredDate;
            return this;
        }

        public Users build() {
            return new Users(this);
        }
    }
}
