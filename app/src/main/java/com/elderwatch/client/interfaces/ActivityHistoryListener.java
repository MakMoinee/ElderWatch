package com.elderwatch.client.interfaces;

import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.Patients;
import com.github.MakMoinee.library.interfaces.DefaultEventListener;

public interface ActivityHistoryListener extends DefaultEventListener {
    default void clickActivityHistoryItem(Patients patients, ActivityHistory history){

    }
}
