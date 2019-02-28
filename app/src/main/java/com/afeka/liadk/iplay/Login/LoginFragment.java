package com.afeka.liadk.iplay.Login;
/*
 *Created by liadk
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private TextView mMail, mPass;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mMail = view.findViewById(R.id.mail_login);
        mPass = view.findViewById(R.id.password_login);
        view.findViewById(R.id.register_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Goto register fragment
                Fragment registerFrag = new RegisterFragment();
                String mail = mMail.getText().toString().trim();
                if (!TextUtils.isEmpty(mail)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RegisterFragment.MAIL_SAVER, mail);
                    registerFrag.setArguments(bundle);
                }
                getActivity().getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.main_login, registerFrag).addToBackStack(null).commit();
            }
        });
        view.findViewById(R.id.rest_password_login).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Goto rest password fragment
                        Fragment restPassFrag = new RestPasswordFragment();
                        String mail = mMail.getText().toString().trim();
                        if (!TextUtils.isEmpty(mail)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(RestPasswordFragment.MAIL_SAVER, mail);
                            restPassFrag.setArguments(bundle);
                        }
                        getActivity().getSupportFragmentManager().beginTransaction().
                                setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.main_login, restPassFrag).addToBackStack(null).commit();
                    }
                });
        view.findViewById(R.id.login_button).setOnClickListener(this);
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        return view;
    }

    @Override
    public void onClick(View view) {
        String mail = mMail.getText().toString().trim();
        String pass = mPass.getText().toString().trim();
        if ((!TextUtils.isEmpty(mail)) && (!TextUtils.isEmpty(pass))) {
            //Mail and password is not empty
            mProgressDialog.show();
            MainActivity.firebaseAuth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {//user is login
                                MainActivity.CurrentUser = MainActivity.firebaseAuth.getCurrentUser();
                                if (!MainActivity.CurrentUser.isEmailVerified()) {
                                    //Goto mail verify fragment
                                    Fragment mailVerificationFrag = new MailVerificationFragment();
                                    getActivity().getSupportFragmentManager().beginTransaction().
                                            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                            .replace(R.id.main_login, mailVerificationFrag).commit();
                                } else {
                                    //Return to main activity
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(intent);
                                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    getActivity().finish();
                                }
                            } else {
                                //Fail to login
                                try {
                                    throw task.getException();
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (FirebaseFirestoreException e) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.authentication_failed, Toast.LENGTH_LONG).show();
                                }
                            }
                            mProgressDialog.cancel();
                        }
                    });
        } else {
            //Mail or password is empty
            Toast.makeText(getContext(), R.string.enter_mail_pass, Toast.LENGTH_LONG).show();
        }
    }
}
