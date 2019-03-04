package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;

public class TournamentActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static LocationPermission currentFragment;

    interface LocationPermission {
        void listenerToLocation();
    }

    public static void requestLocation(LocationPermission locationPermission){
        currentFragment = locationPermission;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            TournamentInfo tournamentInfo = (TournamentInfo) bundle.getSerializable(TournamentDataFragment.REGISTERED);
            if (tournamentInfo != null) {
                TournamentDataFragment tournamentData = new TournamentDataFragment();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    currentFragment.listenerToLocation();
                }
            }
        }
    }

    public boolean checkPermissionLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                final Activity activity = this;
                new AlertDialog.Builder(this, R.style.AlertDialogTheme).setTitle(R.string.title_gps).setMessage(R.string.message_gps)
                        .setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION);
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }
}
