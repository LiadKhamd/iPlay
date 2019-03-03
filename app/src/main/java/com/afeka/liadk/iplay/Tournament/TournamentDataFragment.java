package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.CloudFirestoreConst;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TournamentDataFragment extends Fragment implements CloudFirestoreConst {

    public static final String REGISTERED = "REGISTERED";
    public static final String REVIEW_TOURNAMENT = "REVIEW TOURNAMENT";
    private TextView mTitle, mCity, mPlace, mSport, mTime, mPlayers;
    private TournamentInfo mTournamentInfo;
    private Button mButton;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_data, container, false);
        Bundle bundle = getArguments();
        mTitle = view.findViewById(R.id.registered_tournament_text);
        mButton = view.findViewById(R.id.join_or_cancel_tournament_button);
        mCity = view.findViewById(R.id.city_data_tournament);
        mPlace = view.findViewById(R.id.place_data_tournament);
        mSport = view.findViewById(R.id.sport_data_tournament);
        mTime = view.findViewById(R.id.time_data_tournament);
        mPlayers = view.findViewById(R.id.participates_data_tournament);
        if (bundle != null) {
            mTournamentInfo = (TournamentInfo) bundle.getSerializable(REGISTERED);
            if (mTournamentInfo != null) {//The user is register to this tournament
                registeredToTournament();
                setTournamentData();
            } else {
                mTournamentInfo = (TournamentInfo) bundle.getSerializable(REVIEW_TOURNAMENT);
                if (mTournamentInfo != null) {// The user want to join this tournament
                    reviewTournament();
                    setTournamentData();
                }
            }
        } else {
            ///////////////////////////TO DO
        }
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        return view;
    }

    private void registeredToTournament() {
        mTitle.setText(R.string.registered_tournament);
        mButton.setBackground(getResources().getDrawable(R.drawable.button_delete_or_leave_style));
        if (mTournamentInfo.getmCreatorUsername().compareTo(MainActivity.CurrentUser.getDisplayName()) == 0) {
            //I am the author of this tournament
            mButton.setText(R.string.delete);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteTournament();
                }
            });
        } else {
            mButton.setText(R.string.leave);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leaveTournament();
                }
            });
        }
    }

    private void deleteTournament() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        builder.setTitle(R.string.delete).setMessage(R.string.delete_this_tournament)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgressDialog.show();
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
                String newDateStr = dateFormater.format(date);
                FirebaseFirestore.getInstance().collection(EVENT)
                        .document(CITY).collection(mTournamentInfo.getmCity())
                        .document(SPORT).collection(mTournamentInfo.getmSport())
                        .document(DATE).collection(newDateStr)
                        .document(mTournamentInfo.getmKey() + "")
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), R.string.tournament_has_deleted, Toast.LENGTH_LONG).show();
                        mProgressDialog.cancel();
                        Fragment tournamentMenu = new TournamentMenuFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.tournament_layout, tournamentMenu).commit();
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
                            Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).create().show();
    }

    private void leaveTournament() {

    }

    private void reviewTournament() {
        mTitle.setText(R.string.join_tournament);
        mButton.setBackground(getResources().getDrawable(R.drawable.button_join_style));
        mButton.setText(R.string.join);
    }

    private void joinToTournament() {

    }

    private void setTournamentData() {
        mCity.setText(getContext().getResources().getString(R.string.city) + ": " + mTournamentInfo.getmCity());
        mPlace.setText(getContext().getResources().getString(R.string.place) + ": " + mTournamentInfo.getmPlace());
        mSport.setText(getContext().getResources().getString(R.string.sport) + ": " + mTournamentInfo.getmSport());
        Date date = new Date(mTournamentInfo.getmTime());
        if (date.getMinutes() < 10)
            mTime.setText(getContext().getResources().getString(R.string.time) + ": " + date.getHours() + ":0" + date.getMinutes());
        else
            mTime.setText(getContext().getResources().getString(R.string.time) + ": " + date.getHours() + ":" + date.getMinutes());
        mPlayers.setText(getContext().getResources().getString(R.string.players) + ": " + mTournamentInfo.getPlayers() + "/" + mTournamentInfo.getmMaxParticipants());
    }
}
