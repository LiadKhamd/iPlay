package com.afeka.liadk.iplay;

/*
  Created by liadk
 */

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    final int SPLASH_TIME_OUT = 1000;

//    public static FirebaseAuth firebaseAuth;
//    public static FirebaseUser CurrentUser;

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

    private void setState() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
