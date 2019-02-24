package com.afeka.liadk.iplay.Login;
/*
 *Created by liadk
 */

import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    public static final String MAIL_SAVER = "Mail saver";

    private TextView mMail, mPass;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        Bundle bundle = this.getArguments();
        mMail = view.findViewById(R.id.mail_register);
        mPass = view.findViewById(R.id.password_register);
        if (bundle != null) {
            String mail = bundle.getString(MAIL_SAVER, null);
            if (mail != null)
                mMail.setText(mail);
        }
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);

        view.findViewById(R.id.register_button).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        final String mail = mMail.getText().toString().trim();
        final String pass = mPass.getText().toString().trim();
        if ((!TextUtils.isEmpty(mail)) && (!TextUtils.isEmpty(pass))) {
            //Mail and password isn't empty
            mProgressDialog.show();
            MainActivity.firebaseAuth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                MainActivity.CurrentUser = MainActivity.firebaseAuth.getCurrentUser();
                                MainActivity.CurrentUser.sendEmailVerification();
                                Fragment mailVerFrag = new MailVerificationFragment();
                                getActivity().getSupportFragmentManager().beginTransaction().
                                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                        .replace(R.id.main_login, mailVerFrag).commit();
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (FirebaseFirestoreException e) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(getContext(), R.string.weak_password, Toast.LENGTH_LONG).show();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(getContext(), R.string.email_badly_formatted, Toast.LENGTH_LONG).show();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(getContext(), R.string.mail_in_use, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
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
