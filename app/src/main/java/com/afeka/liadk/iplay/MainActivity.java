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
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;
import com.afeka.liadk.iplay.Tournament.Logic.UserTournamentRegister;
import com.afeka.liadk.iplay.Tournament.TournamentActivity;
import com.afeka.liadk.iplay.Tournament.TournamentDataFragment;
import com.afeka.liadk.iplay.UserProfile.Logic.UserData;
import com.afeka.liadk.iplay.UserProfile.UserProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FireBaseConst {

    final int SPLASH_TIME_OUT = 500;

    private FirebaseAuth mFirebaseAuth;

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
        FirebaseApp.initializeApp(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void setState() {
        final FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser == null) {// User not connect -> goto login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            currentUser.reload().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //problem goto login
                    mFirebaseAuth.signOut();
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (!currentUser.isEmailVerified()) {
                        //mail isn't verify
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        intent.putExtra(LoginActivity.NO_MAIL_VERIFIED, new Bundle());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else if (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()) {
                        //update user profile -> first time create username
                        Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
                        intent.putExtra(UserProfileActivity.FIRST_TIME, new Bundle());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        //goto main app
                        FirebaseFirestore.getInstance().collection(USERS)
                                .document(currentUser.getDisplayName())
                                .get().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mFirebaseAuth.signOut();
                                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                UserData userData = documentSnapshot.toObject(UserData.class);
                                UserTournamentRegister userTournamentRegister = userData.getmEvent();
                                if (userTournamentRegister == null) {
                                    Intent intent = new Intent(getBaseContext(), TournamentActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                } else {
                                    final Date date = new Date(System.currentTimeMillis());
                                    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                                    final String newDateStr = dateFormat.format(date);
                                    FirebaseFirestore.getInstance().collection(EVENT)
                                            .document(CITY).collection(userTournamentRegister.getmCity())
                                            .document(SPORT).collection(userTournamentRegister.getmSport())
                                            .document(DATE).collection(newDateStr)
                                            .document(userTournamentRegister.getmEventKey() + "").get()
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mFirebaseAuth.signOut();
                                                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                                                    startActivity(intent);
                                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                    finish();
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Intent intent = new Intent(getBaseContext(), TournamentActivity.class);
                                            if (documentSnapshot != null) {
                                                TournamentInfo tournamentInfo = documentSnapshot.toObject(TournamentInfo.class);
                                                if (tournamentInfo != null) {
                                                    Date dateTournament = new Date(tournamentInfo.getmTime());
                                                    Date currentDate = new Date(System.currentTimeMillis());
                                                    if (dateTournament.compareTo(currentDate) > 0) {
                                                        Bundle bundle = new Bundle();
                                                        bundle.putSerializable(TournamentDataFragment.REGISTERED, tournamentInfo);
                                                        intent.putExtras(bundle);
                                                    }
                                                } else {
                                                    Map<String, Object> updates = new HashMap<>();
                                                    updates.put("mEvent", null);
                                                    FirebaseFirestore.getInstance().collection(USERS)
                                                            .document(currentUser.getDisplayName()).update(updates);
                                                }
                                                startActivity(intent);
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
