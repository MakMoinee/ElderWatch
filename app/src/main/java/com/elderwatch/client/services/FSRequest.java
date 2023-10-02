package com.elderwatch.client.services;

import com.elderwatch.client.models.Users;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FSRequest extends FirestoreRequest {

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

                        if (users != null) {
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
