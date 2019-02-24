package com.afeka.liadk.iplay;
/*
 *Created by liadk
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.Login.LoginActivity;
import com.afeka.liadk.iplay.UserProfile.UserProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
            CurrentUser.reload().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //problem goto login
                    firebaseAuth.signOut();
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (!CurrentUser.isEmailVerified()) {
                        //mail isn't verify
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        intent.putExtra(LoginActivity.NO_MAIL_VERIFIED, new Bundle());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else if (CurrentUser.getDisplayName() == null || CurrentUser.getDisplayName().isEmpty()) {
                        //update user profile -> first time create username
                        Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
                        intent.putExtra(UserProfileActivity.FIRST_TIME, new Bundle());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        //goto main app
//                        Intent intent = new Intent(getBaseContext(), AppActivity.class);
//                        startActivity(intent);
//                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                        finish();
                    }
                }
            });
        }
    }

    public static void logout() {
        firebaseAuth.signOut();
    }
}
