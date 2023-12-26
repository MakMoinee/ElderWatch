package com.elderwatch.client.interfaces;

import com.elderwatch.client.models.Patients;
import com.github.MakMoinee.library.interfaces.DefaultEventListener;

public interface PatientListener extends DefaultEventListener {

    default void onLongClickListener(Patients patients){

    }
}
