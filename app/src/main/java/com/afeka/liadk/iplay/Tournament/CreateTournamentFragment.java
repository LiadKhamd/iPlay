package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.UserProfile.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Calendar;
import java.util.Date;


public class CreateTournamentFragment extends Fragment implements View.OnClickListener {

    private EditText mCity, mPlace, mSport, mMaxParticipants, mCode;
    private TextView mTime;
    private Switch mPrivate;
    private ProgressDialog mProgressDialog;
    private CollectionReference mCollectionReferenceEventCurrentUser, mCollectionReferenceEvent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_tournament, container, false);
        mCity = view.findViewById(R.id.city_tournament);
        mPlace = view.findViewById(R.id.place_tournament);
        mSport = view.findViewById(R.id.sport_tournament);
        mTime = view.findViewById(R.id.time_tournament);
        mMaxParticipants = view.findViewById(R.id.max_players_tournament);
        mPrivate = view.findViewById(R.id.private_switch_tournament);
        mCode = view.findViewById(R.id.code_tournament);
        view.findViewById(R.id.create_tournament_button).setOnClickListener(this);
        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date currentTime = Calendar.getInstance().getTime();

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTime.setText(hourOfDay + ":" + minute);
                                    }
                                });
                            }
                        }, currentTime.getHours(), currentTime.getMinutes(), true);
                timePickerDialog.show();
            }
        });
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mCollectionReferenceEvent = FirebaseFirestore.getInstance().collection("events");
        mCollectionReferenceEventCurrentUser = FirebaseFirestore.getInstance().collection("users");
        return view;
    }

    @Override
    public void onClick(View view) {
        boolean isValidData = false;
        String tempMaxParticipants = mMaxParticipants.getText().toString().trim();
        if (!TextUtils.isEmpty(tempMaxParticipants)) {
            String temptime = mTime.getText().toString().trim();
            if (!TextUtils.isEmpty(temptime)) {
                int maxParticipants;
                long time;
                maxParticipants = Integer.parseInt(tempMaxParticipants);
                String[] splitTime = temptime.split(":");
                Date date = new Date(System.currentTimeMillis());
                date.setHours(Integer.parseInt(splitTime[0]));
                date.setMinutes(Integer.parseInt(splitTime[1]));
                time = date.getTime();
                String city, place, sport;
                city = mCity.getText().toString().trim();
                place = mPlace.getText().toString().trim();
                sport = mSport.getText().toString().trim();
                if ((!TextUtils.isEmpty(city)) && (!TextUtils.isEmpty(place)) && (!TextUtils.isEmpty(sport)) && (!TextUtils.isEmpty(String.valueOf(maxParticipants)))) {
                    boolean privateTournament = mPrivate.getShowText();
                    String code = null;
                    boolean codeIsEmpty = false;
                    if (privateTournament) {
                        code = mCode.getText().toString().trim();
                        if (TextUtils.isEmpty(code)) {
                            codeIsEmpty = true;
                        }
                    }
                    if (!codeIsEmpty) {
                        isValidData = true;
                        String username = MainActivity.CurrentUser.getDisplayName();
                        TournamentInfo tournamentInfo = new TournamentInfo(city, place, sport, time, maxParticipants, privateTournament, code, username);
                        uploadTournament(tournamentInfo);
                    }
                }
            }
        }
        if (!isValidData) {
            Toast.makeText(getContext(), R.string.not_valid, Toast.LENGTH_LONG).show();
        }
    }

    private void uploadTournament(final TournamentInfo tournamentInfo) {
        mProgressDialog.show();
        final String key = System.currentTimeMillis() + "";
        mCollectionReferenceEventCurrentUser.document(MainActivity.CurrentUser.getDisplayName()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final UserData userData = documentSnapshot.toObject(UserData.class);
                        userData.setmEvent(key);
                        mCollectionReferenceEvent.document("city").collection(tournamentInfo.getmCity())
                                .document("sport").collection(tournamentInfo.getmSport())
                                .document(key).set(tournamentInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCollectionReferenceEventCurrentUser.document(MainActivity.CurrentUser.getDisplayName())
                                                .set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressDialog.cancel();
                                                Toast.makeText(getContext(), R.string.tournament_has_create, Toast.LENGTH_LONG).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception ex) {
                                                mProgressDialog.cancel();
                                                try {
                                                    throw ex;
                                                } catch (FirebaseNetworkException e) {
                                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                                } catch (FirebaseFirestoreException e) {
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
                                } catch (FirebaseFirestoreException e) {
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
                mProgressDialog.cancel();
                try {
                    throw ex;
                } catch (FirebaseNetworkException e) {
                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                } catch (FirebaseFirestoreException e) {
                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
