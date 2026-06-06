package com.elderwatch.client.interfaces;

public interface LogoutListener {
    void logoutCallFinish();

    void logoutNegativeButton();

    default void navigateToQR() {
    }

    default void navigateToPatients() {

    }

    default void navigateToActivities() {

    }

    default void navigateToDevices() {

    }

}
