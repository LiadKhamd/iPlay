package com.afeka.liadk.iplay;
/*
 *Created by liadk
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    public static final int REQUEST_CODE_READ = 102;
    public static final int REQUEST_CODE_WRITE = 103;
    final int SPLASH_TIME_OUT = 500;

    public static FirebaseAuth firebaseAuth;
    public static FirebaseUser CurrentUser;

    private static ImageReq mImageReq;

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
                        FirebaseFirestore.getInstance().collection(USERS)
                                .document(CurrentUser.getDisplayName())
                                .get().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                firebaseAuth.signOut();
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
                                                    firebaseAuth.signOut();
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
                                                            .document(CurrentUser.getDisplayName()).update(updates);
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

    public static void logout() {
        firebaseAuth.signOut();
    }

    public static boolean checkPermission(final Activity activity, final String permission, final int message, final int requsetCode) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                new AlertDialog.Builder(activity, R.style.AlertDialogTheme).setTitle(R.string.storage_access).setMessage(activity.getString(message))
                        .setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{permission},
                                        requsetCode);
                            }
                        }).setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        requsetCode);
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    public void setStorageListener(ImageReq mImageReq) {
        mImageReq = mImageReq;
    }

    public interface ImageReq {
        void iGetPermissionToRead();

        void iGetPermissionToWrite();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MainActivity.REQUEST_CODE_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if (mImageReq != null)
                        mImageReq.iGetPermissionToRead();
                }
                return;
            }
            case MainActivity.REQUEST_CODE_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if (mImageReq != null)
                        mImageReq.iGetPermissionToWrite();
                }
                return;
            }
        }
    }
}
