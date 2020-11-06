package com.dodola.breakpad.network;

import okhttp3.OkHttpClient;

public class OkHttpClientManager {
    OkHttpClient.Builder  builder;

    private OkHttpClientManager() {
    }

    private final static OkHttpClientManager mINSTANCE = new OkHttpClientManager();

    public static OkHttpClientManager getInstance() {
        return mINSTANCE;
    }

    public void init() {
         builder = new OkHttpClient.Builder();
     }

    public OkHttpClient.Builder getHttpClientBuilder() {
        return builder;
    }
}
