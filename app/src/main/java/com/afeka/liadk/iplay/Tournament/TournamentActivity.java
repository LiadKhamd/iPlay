package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afeka.liadk.iplay.R;

public class TournamentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);
//        findViewById(R.id.create_tournament_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Fragment createTournament = new CreateTournamentFragment();
//                getSupportFragmentManager().beginTransaction().
//                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
//                        .replace(R.id.temp_frame_tournament, createTournament).commit();
//            }
//        });
    }
}
