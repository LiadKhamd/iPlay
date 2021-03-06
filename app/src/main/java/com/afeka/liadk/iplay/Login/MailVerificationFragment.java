package com.afeka.liadk.iplay.Login;
/*
 *Created by liadk
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MailVerificationFragment extends Fragment {

    private final int REFRESH = 1000;

    private boolean mThreadRefresh;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_verification, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        //Animation background
        AnimationDrawable animationDrawable = (AnimationDrawable) view.findViewById(R.id.mainVerificationMail).getBackground();
        animationDrawable.setEnterFadeDuration(1000);
        animationDrawable.setExitFadeDuration(1000);
        animationDrawable.start();
        view.findViewById(R.id.send_again_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAgain();
            }
        });
        view.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mThreadRefresh = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Refresh the page in order to check if mail has verified
                while (mThreadRefresh) {
                    try {
                        mCurrentUser.reload();
                        if (mCurrentUser.isEmailVerified()) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            getActivity().finish();
                        }
                        Thread.sleep(REFRESH);
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mThreadRefresh = false;
    }

    private void sendAgain() {
        //send again mail verification
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        String resendMail = getContext().getString(R.string.mail_resend) + " " + mCurrentUser.getEmail();
        builder.setTitle(R.string.mail_verify).setMessage(resendMail)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCurrentUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), R.string.check_mail, Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                try {
                                    throw e;
                                } catch (FirebaseNetworkException e1) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (Exception e1) {
                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(19);
    }

    private void logout() {
        mFirebaseAuth.signOut();
        Fragment loginFrag = new LoginFragment();
        getActivity().getSupportFragmentManager().beginTransaction().
                setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_login, loginFrag).addToBackStack(null).commit();
    }
}
