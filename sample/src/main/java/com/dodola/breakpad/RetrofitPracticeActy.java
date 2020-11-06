package com.dodola.breakpad;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;

import com.dodola.breakpad.network.GithubClient;
import com.dodola.breakpad.network.RetrofitManager;
import com.dodola.breakpad.network.data.GitHubRepo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitPracticeActy extends Activity {
    private static final String TAG = "RetrofitPracticeActy";
    private static final  String BASEURL_GITHUB = "https://api.github.com/";
    private static final  String BASEURL_BAIDU = "https://www.baidu.com/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(this);
        asyncLayoutInflater.inflate(R.layout.retrofit_practice, null, new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.d(TAG, "onInflateFinished() called with: view = [" + view + "], resid = [" + resid + "], parent = [" + parent + "]");
                init(view);

            }
        });
    }

    public void init(View contentView) {
        setContentView(contentView);
        final GithubClient client = RetrofitManager.getInstance().createClient(GithubClient.class);

        findViewById(R.id.bt_baidu_base_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.getInstance().changeBaseUrl(BASEURL_GITHUB);
                Call<List<GitHubRepo>> zengqiang24 = client.reposForUser("zengqiang24");
                zengqiang24.enqueue(new Callback<List<GitHubRepo>>() {
                    @Override
                    public void onResponse(Call<List<GitHubRepo>> call, Response<List<GitHubRepo>> response) {
                        Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                    }

                    @Override
                    public void onFailure(Call<List<GitHubRepo>> call, Throwable t) {
                        Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                    }
                });
            }
        });

        findViewById(R.id.bt_other_base_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.getInstance().changeBaseUrl(BASEURL_BAIDU);
                Call<List<GitHubRepo>> zengqiang24 = client.reposForUser("zengqiang24");
                zengqiang24.enqueue(new Callback<List<GitHubRepo>>() {
                    @Override
                    public void onResponse(Call<List<GitHubRepo>> call, Response<List<GitHubRepo>> response) {
                        Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                    }

                    @Override
                    public void onFailure(Call<List<GitHubRepo>> call, Throwable t) {
                        Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                    }
                });
            }
        });
    }
}
