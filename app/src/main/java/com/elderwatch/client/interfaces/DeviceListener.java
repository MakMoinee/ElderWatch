package com.elderwatch.client.interfaces;

import com.elderwatch.client.models.Devices;
import com.github.MakMoinee.library.interfaces.DefaultEventListener;

public interface DeviceListener extends DefaultEventListener {

    default void onDeviceClickListener(Devices devices){

    }
}
