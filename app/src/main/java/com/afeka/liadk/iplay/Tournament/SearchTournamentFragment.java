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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.CloudFirestoreConst;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.LocationProvider;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentInfo;
import com.afeka.liadk.iplay.Tournament.Logic.TournamentRecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SearchTournamentFragment extends Fragment implements View.OnClickListener, TournamentRecyclerViewAdapter.ItemClickListener, CloudFirestoreConst, LocationProvider.MyLocation, TournamentActivity.LocationPermission {

    public final int REQUEST_CODE_GPS = 1;

    private EditText mCity, mSport;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private CollectionReference mCollectionReferenceEvent;
    private TournamentRecyclerViewAdapter mAdapter;
    private TextView mWaitForResultTournament;
    private List<TournamentInfo> mDocuments;
    private TournamentRecyclerViewAdapter.ItemClickListener mItemClickListener;
    private LocationProvider mLocationProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_tournament, container, false);
        view.findViewById(R.id.search_tournament_button).setOnClickListener(this);
        mCity = view.findViewById(R.id.city_search_tournament);
        mLocationProvider = new LocationProvider(this, getActivity());
        mSport = view.findViewById(R.id.sport_search_tournament);
        mRecyclerView = view.findViewById(R.id.tournament_recyclerView);
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mCollectionReferenceEvent = FirebaseFirestore.getInstance().collection(EVENT);
        mWaitForResultTournament = view.findViewById(R.id.result_tournament_search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        mAdapter = new TournamentRecyclerViewAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);
        mItemClickListener = this;
        ((TournamentActivity) getActivity()).requestLocation(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        final String city, sport, newDateStr;
        city = mCity.getText().toString().trim().toLowerCase();
        sport = mSport.getText().toString().trim().toLowerCase();
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
        newDateStr = dateFormater.format(date);
        if ((!TextUtils.isEmpty(city)) && (!TextUtils.isEmpty(sport))) {
            mProgressDialog.show();
            mCollectionReferenceEvent.document(CITY).collection(city)
                    .document(SPORT).collection(sport)
                    .document(DATE).collection(newDateStr)
                    .get().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception ex) {
                    mProgressDialog.cancel();
                    try {
                        throw ex;
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                    } catch (FirebaseFirestoreException e) {
                        Toast.makeText(getContext(), R.string.result_not_found, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    mDocuments = removeOldEvent(queryDocumentSnapshots.getDocuments());
                    mAdapter = new TournamentRecyclerViewAdapter(getContext(), mDocuments);
                    mAdapter.setClickListener(mItemClickListener);
                    mRecyclerView.setAdapter(mAdapter);
                    if (mDocuments.size() == 0) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWaitForResultTournament.setVisibility(View.VISIBLE);
                                mWaitForResultTournament.setText(R.string.result_not_found);
                                mDocuments.clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWaitForResultTournament.setVisibility(View.INVISIBLE);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    mProgressDialog.cancel();
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.not_valid, Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<TournamentInfo> removeOldEvent(List<DocumentSnapshot> list) {
        ArrayList<TournamentInfo> tournamentInfos = new ArrayList<>();
        if (list != null && list.size() >= 0) {
            TournamentInfo tournamentInfo;
            Date tournamentDate, currentDate;
            for (int i = 0; i < list.size(); i++) {
                tournamentInfo = list.get(i).toObject(TournamentInfo.class);
                tournamentDate = new Date(tournamentInfo.getmTime());
                currentDate = new Date(System.currentTimeMillis());
                if (tournamentDate.compareTo(currentDate) > 0) {
                    tournamentInfos.add(tournamentInfo);
                }
            }
        }
        return tournamentInfos;
    }

    @Override
    public void onItemClick(View view, int position) {
        final TournamentInfo tournamentInfo = mDocuments.get(position);
        if (tournamentInfo.getPlayers() >= tournamentInfo.getmMaxParticipants()) {
            Toast.makeText(getContext(), R.string.tournament_full, Toast.LENGTH_LONG).show();
            return;
        }
        if (tournamentInfo.ismPrivate()) {
            final EditText editTextCode = new EditText(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            editTextCode.setLayoutParams(params);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
            builder.setView(editTextCode).setTitle(R.string.is_private_tournament).setMessage(R.string.enter_private_code)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (editTextCode.getText().length() != 0) {
                        if (editTextCode.getText().toString().compareTo(tournamentInfo.getmCode()) == 0) {
                            changeToTournamentInfo(tournamentInfo);
                        } else {
                            Toast.makeText(getContext(), R.string.invalid_code, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.empty_code, Toast.LENGTH_LONG).show();
                    }
                }
            }).create().show();
        } else {
            changeToTournamentInfo(tournamentInfo);
        }
    }

    private void changeToTournamentInfo(TournamentInfo tournamentInfo) {
        Fragment tournamentDataFragment = new TournamentDataFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TournamentDataFragment.REVIEW_TOURNAMENT, tournamentInfo);
        tournamentDataFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.tournament_layout, tournamentDataFragment).addToBackStack(null).commit();
    }

    @Override
    public void myCityName(final String cityName) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCity.setText(cityName);
            }
        });
    }

    @Override
    public boolean checkPermission() {
        return ((TournamentActivity) getActivity()).checkPermissionLocation();
    }

    @Override
    public void listenerToLocation() {
        mLocationProvider.startSearchLocation();
    }
}
