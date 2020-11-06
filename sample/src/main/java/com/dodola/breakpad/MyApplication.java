package com.dodola.breakpad;

import android.app.Application;

import com.dodola.breakpad.network.OkHttpClientManager;
import com.dodola.breakpad.network.RetrofitManager;

import okhttp3.OkHttpClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClientManager.getInstance().init();
        OkHttpClient.Builder httpClientBuilder = OkHttpClientManager.getInstance().getHttpClientBuilder();
        RetrofitManager.getInstance().init(httpClientBuilder);
    }
}
