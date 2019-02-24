package com.afeka.liadk.iplay.Login;
/*
 *Created by liadk
 */

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class RestPasswordFragment extends Fragment {

    public static final String MAIL_SAVER = "Mail saver";

    private TextView mMail;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rest_password, container, false);
        Bundle bundle = this.getArguments();
        mMail = view.findViewById(R.id.mail_rest);
        if (bundle != null) {
            String mail = bundle.getString(MAIL_SAVER, null);
            if (mail != null)
                mMail.setText(mail);
        }
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        view.findViewById(R.id.rest_pass_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = mMail.getText().toString().trim();
                if (!TextUtils.isEmpty(mail)) {
                    //send rest password mail
                    mProgressDialog.show();
                    MainActivity.firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressDialog.cancel();
                            getActivity().getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            Toast.makeText(getContext(), R.string.check_mail, Toast.LENGTH_LONG).show();
                            Fragment loginFrag = new LoginFragment();
                            getActivity().getSupportFragmentManager().beginTransaction().
                                    setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                    .replace(R.id.main_login, loginFrag).commit();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.cancel();
                            Toast.makeText(getContext(), R.string.wrong_mail, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    //mail is empty
                    Toast.makeText(getContext(), R.string.enter_mail, Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }
}
