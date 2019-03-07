package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.FireBaseConst;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.PlayersRecyclerViewAdapter;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;
import com.afeka.liadk.iplay.Tournament.Logic.UserTournamentRegister;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TournamentDataFragment extends Fragment implements FireBaseConst {

    private final int REQUEST_CODE_WRITE = 103;
    private final int REFRESH = 5000;

    public static final String REGISTERED = "REGISTERED";
    public static final String REVIEW_TOURNAMENT = "REVIEW TOURNAMENT";

    private TextView mTitle, mCity, mPlace, mSport, mTime, mPlayers;
    private TournamentInfo mTournamentInfo;
    private Button mButton;
    private ProgressDialog mProgressDialog;
    private boolean mThreadRefresh;
    private PlayersRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private boolean mPermission;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseFirestore mFirebaseFirestore;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_data, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme).setTitle(R.string.storage_access)
                    .setMessage(R.string.write_permission)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_WRITE
                            );
                        }
                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        } else {
            mPermission = true;
        }
        Bundle bundle = getArguments();
        mRecyclerView = view.findViewById(R.id.username_in_tournament_recyclerview);
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
            Fragment menuFragment = new TournamentMenuFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tournament_layout, menuFragment).commit();
            return view;
        }
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mContext = getContext();
        return view;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    mPermission = true;
                    setUsersData();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Update tournament data
        if (mTournamentInfo != null) {
            mThreadRefresh = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Refresh the page in order to update tournament
                    while (mThreadRefresh) {
                        try {
                            Date date = new Date(System.currentTimeMillis());
                            SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
                            String newDateStr = dateFormater.format(date);
                            Date tournamentDate = new Date(mTournamentInfo.getmTime());
                            Log.e("mimi1", mTournamentInfo.getmTime() + " " + System.currentTimeMillis() + " " + (mTournamentInfo.getmTime() - System.currentTimeMillis()));

                            if (mTournamentInfo.getmTime() - System.currentTimeMillis() > 0) {
                                Log.e("mimi2", mTournamentInfo.getmTime() + " " + System.currentTimeMillis() + " " + (mTournamentInfo.getmTime() - System.currentTimeMillis()));
                                mFirebaseFirestore.
                                        collection(EVENT)
                                        .document(CITY).collection(mTournamentInfo.getmCity())
                                        .document(SPORT).collection(mTournamentInfo.getmSport())
                                        .document(DATE).collection(newDateStr)
                                        .document(mTournamentInfo.getmKey() + "").get().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception ex) {
                                        try {
                                            throw ex;
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot != null && documentSnapshot.exists()) {
                                            TournamentInfo tournamentInfo = documentSnapshot.toObject(TournamentInfo.class);
                                            if (!mTournamentInfo.isSamePlayers(tournamentInfo)) {
                                                mTournamentInfo = tournamentInfo;
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setTournamentData();
                                                    }
                                                });
                                            }
                                        } else {
                                            Toast.makeText(getContext(), R.string.tournament_has_deleted, Toast.LENGTH_LONG).show();
                                            Fragment menuFragment = new TournamentMenuFragment();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tournament_layout, menuFragment).commit();
                                        }
                                    }
                                });
                                Thread.sleep(REFRESH);
                            } else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, R.string.tournament_end, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Fragment menuFragment = new TournamentMenuFragment();
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tournament_layout, menuFragment).commit();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mThreadRefresh = false;
    }

    private void registeredToTournament() {
        mTitle.setText(R.string.registered_tournament);
        mButton.setBackground(getResources().getDrawable(R.drawable.button_delete_or_leave_style));
        if (mTournamentInfo.getmCreatorUsername().compareTo(mCurrentUser.getDisplayName()) == 0) {
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
                mFirebaseFirestore.collection(EVENT)
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
                        } catch (Exception e) {
                            Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).create().show();
    }

    private void reviewTournament() {
        mTitle.setText(R.string.join_tournament);
        mButton.setBackground(getResources().getDrawable(R.drawable.button_join_style));
        mButton.setText(R.string.join);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinToTournament();
            }
        });
    }

    private void leaveTournament() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        builder.setTitle(R.string.leave).setMessage(R.string.delete_this_tournament)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgressDialog.show();
                mTournamentInfo.removePlayer(mCurrentUser.getDisplayName());
                Map<String, Object> updates = new HashMap<>();
                updates.put("mParticipantsUsersnames", mTournamentInfo.getmParticipantsUsersnames());
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
                String newDateStr = dateFormater.format(date);
                mFirebaseFirestore.collection(EVENT)
                        .document(CITY).collection(mTournamentInfo.getmCity())
                        .document(SPORT).collection(mTournamentInfo.getmSport())
                        .document(DATE).collection(newDateStr)
                        .document(mTournamentInfo.getmKey() + "").update(updates)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception ex) {
                                mProgressDialog.cancel();
                                try {
                                    throw ex;
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String, Object> updatesMe = new HashMap<>();
                        updatesMe.put("mEvent", null);
                        mFirebaseFirestore.collection(USERS)
                                .document(mCurrentUser.getDisplayName()).update(updatesMe)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProgressDialog.cancel();
                                        Toast.makeText(getContext(), R.string.you_leave_tournament, Toast.LENGTH_LONG).show();
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
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }
        }).create().show();
    }

    private void joinToTournament() {
        if (mTournamentInfo.getPlayers() < mTournamentInfo.getmMaxParticipants()) {
            if (mTournamentInfo.addPlayer(mCurrentUser.getDisplayName())) {
                mProgressDialog.show();
                Map<String, Object> updates = new HashMap<>();
                updates.put("mParticipantsUsersnames", mTournamentInfo.getmParticipantsUsersnames());
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
                String newDateStr = dateFormater.format(date);
                mFirebaseFirestore.collection(EVENT)
                        .document(CITY).collection(mTournamentInfo.getmCity())
                        .document(SPORT).collection(mTournamentInfo.getmSport())
                        .document(DATE).collection(newDateStr)
                        .document(mTournamentInfo.getmKey() + "").update(updates).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        mProgressDialog.cancel();
                        try {
                            throw ex;
                        } catch (FirebaseNetworkException e) {
                            Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> updatesMe = new HashMap<>();
                        updatesMe.put("mEvent", new UserTournamentRegister(mTournamentInfo.getmCity(), mTournamentInfo.getmSport(), mTournamentInfo.getmKey() + ""));
                        mFirebaseFirestore.collection(USERS)
                                .document(mCurrentUser.getDisplayName()).update(updatesMe)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProgressDialog.cancel();
                                        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                        Toast.makeText(getContext(), R.string.add_success, Toast.LENGTH_LONG).show();
                                        Fragment tournamentDataFragment = new TournamentDataFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(TournamentDataFragment.REGISTERED, mTournamentInfo);
                                        tournamentDataFragment.setArguments(bundle);
                                        getActivity().getSupportFragmentManager().popBackStack(null,
                                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            } else {
                Toast.makeText(getContext(), R.string.already_register, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), R.string.tournament_full, Toast.LENGTH_LONG).show();
        }
    }

    private void setTournamentData() {
        mCity.setText(mTournamentInfo.getmCity());
        mPlace.setText(mTournamentInfo.getmPlace());
        mSport.setText(mTournamentInfo.getmSport());
        Date date = new Date(mTournamentInfo.getmTime());
        if (date.getMinutes() < 10)
            mTime.setText(date.getHours() + ":0" + date.getMinutes());
        else
            mTime.setText(date.getHours() + ":" + date.getMinutes());
        mPlayers.setText(mTournamentInfo.getPlayers() + "/" + mTournamentInfo.getmMaxParticipants());
        setUsersData();
    }

    private void setUsersData() {
        mAdapter = new PlayersRecyclerViewAdapter(getContext(), mTournamentInfo.getmParticipantsUsersnames(), mPermission);
        mRecyclerView.setAdapter(mAdapter);
    }
}
