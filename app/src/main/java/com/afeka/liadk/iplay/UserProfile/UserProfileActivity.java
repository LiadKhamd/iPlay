package com.afeka.liadk.iplay.UserProfile;
/*
 *Created by liadk
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.R;

public class UserProfileActivity extends AppCompatActivity {

    public static final String FIRST_TIME = "Create new user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Intent intent = getIntent();
        Bundle firstTime = intent.getBundleExtra(FIRST_TIME);
        if (firstTime != null) {//Create new user
            Fragment newUser = new NewUserFragment();
            getSupportFragmentManager().beginTransaction().
                    setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.user_profile, newUser)
                    .commit();
        } else {//Update user profile image
            Fragment updateProfileImage = new UserImageProfileFragment();
            getSupportFragmentManager().beginTransaction().
                    setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.user_profile, updateProfileImage)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
