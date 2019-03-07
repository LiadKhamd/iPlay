package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.UserProfile.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;


public class TournamentMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_menu, container, false);
        view.findViewById(R.id.create_a_tournament).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment createTournament = new CreateTournamentFragment();
                getActivity().getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.tournament_layout, createTournament).addToBackStack(null).commit();
            }
        });
        view.findViewById(R.id.search_for_tournament).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment searchTournament = new SearchTournamentFragment();
                getActivity().getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.tournament_layout, searchTournament).addToBackStack(null).commit();
            }
        });
        view.findViewById(R.id.my_proflie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        view.findViewById(R.id.logout_tournament).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                        .setTitle(R.string.logout).setMessage(R.string.confirm_logout)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                startActivity(intent);
                                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                getActivity().finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                TextView textView = (TextView) alert.findViewById(android.R.id.message);
                textView.setTextSize(19);
            }
        });
        return view;
    }
}
