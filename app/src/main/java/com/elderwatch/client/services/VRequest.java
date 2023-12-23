package com.elderwatch.client.services;

import android.content.Context;

import com.github.MakMoinee.library.services.LocalVolleyRequest;

public class VRequest extends LocalVolleyRequest {

    private static final String serverIP = "192.168.1.9";
    public static final String pingCameraURLString ="http://" + serverIP + ":5000/ping?ip=%s&id=%s";
    public static final String startCameraURLString ="http://" + serverIP + ":5000/start?ip=%s&id=%s";
    public VRequest(Context mContext) {
        super(mContext);
    }
}
