package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afeka.liadk.iplay.FireBaseConst;
import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.LocationProvider;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;
import com.afeka.liadk.iplay.UserProfile.Logic.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CreateTournamentFragment extends Fragment implements View.OnClickListener, FireBaseConst, LocationProvider.MyLocation, TournamentActivity.LocationPermission {

    public final int REQUEST_CODE_GPS = 2;

    private EditText mCity, mPlace, mSport, mMaxParticipants, mCode;
    private TextView mTime;
    private Switch mPrivate;
    private RelativeLayout mPrivateRelativeLayout;
    private ProgressDialog mProgressDialog;
    private CollectionReference mCollectionReferenceEventCurrentUser, mCollectionReferenceEvent;
    private LocationProvider mLocationProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_tournament, container, false);
        mCity = view.findViewById(R.id.city_tournament);
        mLocationProvider = new LocationProvider(this, getActivity());
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
                                        if (minute < 10)
                                            mTime.setText(hourOfDay + ":" + "0" + minute);
                                        else
                                            mTime.setText(hourOfDay + ":" + minute);
                                    }
                                });
                            }
                        }, currentTime.getHours(), currentTime.getMinutes(), false);
                timePickerDialog.show();
            }
        });
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mCollectionReferenceEvent = FirebaseFirestore.getInstance().collection(EVENT);
        mCollectionReferenceEventCurrentUser = FirebaseFirestore.getInstance().collection(USERS);
        mPrivateRelativeLayout = view.findViewById(R.id.private_tournament_layout);
        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPrivateRelativeLayout.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPrivateRelativeLayout.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
        ((TournamentActivity) getActivity()).requestLocation(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        boolean isValidData = false;
        String city, place, sport, maxParticipantsString, timeString, code = null;
        city = mCity.getText().toString().trim().toLowerCase();
        place = mPlace.getText().toString().trim().toLowerCase();
        sport = mSport.getText().toString().trim().toLowerCase();
        timeString = mTime.getText().toString().trim();
        maxParticipantsString = mMaxParticipants.getText().toString().trim();
        boolean privateTournament = mPrivate.isChecked();
        if ((!TextUtils.isEmpty(city)) && (!TextUtils.isEmpty(place)) && (!TextUtils.isEmpty(sport)) && !TextUtils.isEmpty(maxParticipantsString) && !TextUtils.isEmpty(timeString)) {
            if (privateTournament) {
                //Private tournament
                code = mCode.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(getContext(), R.string.code_empty, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            long time, currentTime = System.currentTimeMillis();
            String[] splitTime = timeString.split(":");
            Date date = new Date(System.currentTimeMillis());
            date.setHours(Integer.parseInt(splitTime[0]));
            date.setMinutes(Integer.parseInt(splitTime[1]));
            time = date.getTime();
            if (time - currentTime <= 0) {
                //Check time
                Toast.makeText(getContext(), R.string.time_not_valid, Toast.LENGTH_LONG).show();
                return;
            }
            int maxParticipants = Integer.parseInt(maxParticipantsString);
            if (maxParticipants < 2) {
                //Check participants
                Toast.makeText(getContext(), R.string.max_player_not_valid, Toast.LENGTH_LONG).show();
                return;
            }
            String username = MainActivity.CurrentUser.getDisplayName();
            TournamentInfo tournamentInfo = new TournamentInfo(city, place, sport, time, maxParticipants, privateTournament, code, username, System.currentTimeMillis());
            uploadTournament(tournamentInfo);
        } else
            Toast.makeText(getContext(), R.string.not_valid, Toast.LENGTH_LONG).show();
    }

    private void uploadTournament(final TournamentInfo tournamentInfo) {
        mProgressDialog.show();
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String newDateStr = dateFormat.format(date);
        mCollectionReferenceEventCurrentUser.document(MainActivity.CurrentUser.getDisplayName()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final UserData userData = documentSnapshot.toObject(UserData.class);
                        userData.setmEvent(tournamentInfo.getmCity(), tournamentInfo.getmSport(), tournamentInfo.getmKey() + "");
                        mCollectionReferenceEvent.document(CITY).collection(tournamentInfo.getmCity())
                                .document(SPORT).collection(tournamentInfo.getmSport())
                                .document(DATE).collection(newDateStr)
                                .document(tournamentInfo.getmKey() + "").set(tournamentInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCollectionReferenceEventCurrentUser.document(MainActivity.CurrentUser.getDisplayName())
                                                .set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressDialog.cancel();
                                                Toast.makeText(getContext(), R.string.tournament_has_create, Toast.LENGTH_LONG).show();
                                                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                Fragment tournamentDataFragment = new TournamentDataFragment();
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable(TournamentDataFragment.REGISTERED, tournamentInfo);
                                                tournamentDataFragment.setArguments(bundle);
                                                getActivity().getSupportFragmentManager().beginTransaction()
                                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                                        .replace(R.id.tournament_layout, tournamentDataFragment).commit();
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

    @Override
    public void myCityName(final String cityName) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCity.setText(cityName);
            }
        });
    }

    @Override
    public boolean checkPermission() {
        return ((TournamentActivity) getActivity()).checkPermissionLocation();
    }

    @Override
    public void listenerToLocation() {
        mLocationProvider.startSearchLocation();
    }
}
