package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.R;

public class TournamentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);
        Fragment menuFragment = new TournamentMenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.tournament_layout, menuFragment).commit();
    }
}
