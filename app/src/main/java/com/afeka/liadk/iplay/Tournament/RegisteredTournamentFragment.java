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


public class RegisteredTournamentFragment extends Fragment {

    public final static String TOURNAMENT = "TOURNAMENT INFO";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registered_tournament, container, false);
        return view;
    }
}
