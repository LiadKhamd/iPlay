package com.afeka.liadk.iplay.Login;
/*
 *Created by liadk
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.R;

public class LoginActivity extends AppCompatActivity {

    public static final String NO_MAIL_VERIFIED = "Mail not verify";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        Bundle noMailVerifiedBundle = intent.getBundleExtra(NO_MAIL_VERIFIED);
        if (noMailVerifiedBundle != null) {//Check if the user is not verified the mail
            Fragment mailVerFragment = new MailVerificationFragment();
            getSupportFragmentManager().beginTransaction().
                    setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.main_login, mailVerFragment)
                    .commit();
        } else {//Set login page fragment
            Fragment loginFrag = new LoginFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_login, loginFrag).commit();
        }
    }

//    public static AlertDialog.Builder createDialog(Context context, int message) {//Create dialog with theme
//        return new AlertDialog.Builder(context, R.style.AlertDialogTheme).setMessage(context.getString(message));
//    }

//    public static int createCustomTransaction(Context context, Fragment fragment) {//Create default transaction
//        return ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().
//                setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.main_login, fragment).addToBackStack(null).commit();
//    }
}
