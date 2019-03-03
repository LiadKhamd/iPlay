package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;

public class TournamentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            TournamentInfo tournamentInfo = (TournamentInfo) bundle.getSerializable(TournamentDataFragment.REGISTERED);
            if (tournamentInfo != null) {
                Fragment tournamentData = new TournamentDataFragment();
                Bundle bundleTournamentData = new Bundle();
                bundleTournamentData.putSerializable(TournamentDataFragment.REGISTERED, tournamentInfo);
                tournamentData.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.tournament_layout, tournamentData).commit();
                return;
            }
        }
        Fragment menuFragment = new TournamentMenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.tournament_layout, menuFragment).commit();
    }
}
