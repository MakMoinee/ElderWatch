package com.elderwatch.client.services;

import com.elderwatch.client.models.Users;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FSRequest extends FirestoreRequest {

    public static final String DEVICES_COLLECTION = "devices";
    public static final String PG_COLLECTION = "patient_guardian";
    public static final String PATIENTS_COLLECTION = "patients";
    public static final String ACTIVITY_COLLECTION = "activity";
    public static final String ACTIVITY_HISTORY_COLLECTION = "activity_history";
    public static final String TOKEN_COLLECTION = "tokens";
    public static final String CAREGIVER_ACTIVITY_COLLECTION = "activity";

    public static final String SMS_API_COLLECTION = "keys";

    public FSRequest() {
        super();
    }


    public void login(FirestoreRequestBody body, FirestoreListener listener) {
        this.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof QuerySnapshot) {
                    QuerySnapshot snapshots = (QuerySnapshot) any;
                    if (snapshots.isEmpty()) {
                        listener.onError(new Error("empty"));
                    } else {
                        Users users = null;
                        for (DocumentSnapshot documentSnapshot : snapshots) {
                            if (documentSnapshot.exists()) {
                                users = documentSnapshot.toObject(Users.class);
                                if (users != null) {
                                    users.setUserID(documentSnapshot.getId());
                                    break;
                                }
                            }
                        }

                        if (users != null && !users.getUserID().equals("")) {
                            listener.onSuccess(users);
                        } else {
                            listener.onError(new Error("empty"));
                        }
                    }
                }
            }

            @Override
            public void onError(Error error) {
                listener.onError(error);
            }
        });
    }


}
