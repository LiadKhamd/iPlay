package com.afeka.liadk.iplay.UserProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewUserFragment extends Fragment {

    private EditText mUserName;
    private CollectionReference mCollectionReference;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_user, container, false);
        mUserName = view.findViewById(R.id.username_edit_text);
        mCollectionReference = FirebaseFirestore.getInstance().collection("users");
        view.findViewById(R.id.next_profile_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
//                Intent intent = new Intent(getContext(), AppActivity.class);
//                startActivity(intent);
//                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                getActivity().finish();
            }
        });
        view.findViewById(R.id.logout_profile_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.logout();
                Intent mainActivity = new Intent(getContext(), MainActivity.class);
                startActivity(mainActivity);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                getActivity().finish();
            }
        });
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        return view;
    }

    private void createUser() {
        final String username = mUserName.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {//Empty username
            Toast.makeText(getContext(), R.string.empty_username, Toast.LENGTH_LONG).show();
        } else {
            mProgressDialog.show();
            //check if username is already in used
            mCollectionReference.document(username).get().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception ex) {
                    mProgressDialog.cancel();
                    try {
                        throw ex;
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        //Username is already in used
                        mProgressDialog.cancel();
                        Toast.makeText(getContext(), R.string.username_used, Toast.LENGTH_LONG).show();
                    } else {
                        //Try to set the username
                        UserData userData = new UserData(MainActivity.CurrentUser.getUid());
                        mCollectionReference.document(username).set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Update the current user data
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                MainActivity.CurrentUser.updateProfile(profileUpdates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            //all good
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressDialog.cancel();
                                                getActivity().getSupportFragmentManager().beginTransaction().
                                                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                                        .replace(R.id.user_profile, new UserProfileImageFragment()).commit();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception ex) {
                                        //Problem
                                        mProgressDialog.cancel();
                                        try {
                                            throw ex;
                                        } catch (FirebaseNetworkException e) {
                                            Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception ex) {
                                //Problem
                                mProgressDialog.cancel();
                                try {
                                    throw ex;
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

}
