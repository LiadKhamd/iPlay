package com.afeka.liadk.iplay;

/*
  Created by liadk
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.Login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    final int SPLASH_TIME_OUT = 1000;

    public static FirebaseAuth firebaseAuth;
    public static FirebaseUser CurrentUser;

    @Override
    public void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setState();
            }

        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setState() {
        CurrentUser = firebaseAuth.getCurrentUser();
        if (CurrentUser == null) {// User not connect -> goto login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {

        }
    }
}
