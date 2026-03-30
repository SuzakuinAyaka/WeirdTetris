package com.example.els.ui.launcher;

import android.content.Intent;
import android.os.Bundle;

import com.example.els.ui.base.BaseActivity;
import com.example.els.ui.welcome.WelcomeActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}



