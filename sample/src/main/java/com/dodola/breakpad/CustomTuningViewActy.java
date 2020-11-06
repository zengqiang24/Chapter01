package com.dodola.breakpad;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class CustomTuningViewActy extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view_layout);
    }
}
