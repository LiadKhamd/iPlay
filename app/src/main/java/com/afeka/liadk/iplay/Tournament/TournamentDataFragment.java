package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;


public class TournamentDataFragment extends Fragment {

    public static final String REGISTERED = "REGISTERED";
    public static final String JOIN_TOURNAMENT = "JOIN TOURNAMENT";
    private TournamentInfo mTournamentInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_data, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTournamentInfo = (TournamentInfo) bundle.getSerializable(REGISTERED);
            if (mTournamentInfo != null) {//The user is register to this tournament

            } else {
                mTournamentInfo = (TournamentInfo) bundle.getSerializable(JOIN_TOURNAMENT);
                if (mTournamentInfo != null) {// The user want to join this tournament

                }
            }
        }
        else{
            ///////////////////////////TO DO
        }
        return view;
    }
}
