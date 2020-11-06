package com.dodola.breakpad.network;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private static final String TAG = "RetrofitManager";
    private Retrofit mRetrofit;
    String API_BASE_URL = "https://api.github.com/";
    //    String API_BASE_URL = "https://localhost:8080/";
    private String mScheme;
    private String mHost;
    private static final RetrofitManager mINSTANCE = new RetrofitManager();

    private RetrofitManager() {
    }

    public static RetrofitManager getInstance() {
        return mINSTANCE;
    }


    public void init(OkHttpClient.Builder clientbuilder) {
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );


        clientbuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                HttpUrl newUrl;
                if (!TextUtils.isEmpty(mScheme) && !TextUtils.isEmpty(mHost)) {
                    newUrl = request.url().newBuilder().scheme(mScheme).host(mHost).build();
                } else {
                    newUrl = request.url().newBuilder().build();
                }
                return chain.proceed(request.newBuilder().url(newUrl).build());

            }
        });
        mRetrofit = builder.client(clientbuilder.build()).build();
    }

    public void changeBaseUrl(String newBaseUrl) {
        this.mScheme = HttpUrl.parse(newBaseUrl).scheme();
        this.mHost = HttpUrl.parse(newBaseUrl).host();
    }

    public <T> T createClient(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }
}
