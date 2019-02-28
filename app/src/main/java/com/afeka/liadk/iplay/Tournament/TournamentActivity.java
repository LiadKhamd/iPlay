package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;

public class TournamentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);
        findViewById(R.id.create_a_tournament).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment createTournament = new CreateTournamentFragment();
                getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.tournament_layout, createTournament).commit();
            }
        });
        findViewById(R.id.search_for_tournament).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment createTournament = new CreateTournamentFragment();
                getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.tournament_layout, createTournament).commit();
            }
        });
        findViewById(R.id.my_proflie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment createTournament = new CreateTournamentFragment();
                getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.tournament_layout, createTournament).commit();
            }
        });
        findViewById(R.id.logout_tournament).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TournamentActivity.this, R.style.AlertDialogTheme)
                        .setTitle(R.string.logout).setMessage(R.string.confirm_logout)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.logout();
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                TextView textView = (TextView) alert.findViewById(android.R.id.message);
                textView.setTextSize(19);
            }
        });
    }
}
