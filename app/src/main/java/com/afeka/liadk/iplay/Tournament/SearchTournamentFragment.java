package com.afeka.liadk.iplay.Tournament;
/*
 *Created by liadk
 */

import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.Logic.CloudFirestoreConst;
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
import java.util.Date;
import java.util.List;


public class SearchTournamentFragment extends Fragment implements View.OnClickListener, CloudFirestoreConst {

    private EditText mCity, mSport;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private CollectionReference mCollectionReferenceEvent;
    private TournamentRecyclerViewAdapter mAdapter;
    private TextView mWaitForResultTournament;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_tournament, container, false);
        view.findViewById(R.id.search_tournament_button).setOnClickListener(this);
        mCity = view.findViewById(R.id.city_search_tournament);
        mSport = view.findViewById(R.id.sport_search_tournament);
        mRecyclerView = view.findViewById(R.id.tournament_recyclerView);
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mCollectionReferenceEvent = FirebaseFirestore.getInstance().collection(EVENT);
        mWaitForResultTournament = view.findViewById(R.id.result_tournament_search);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext()
////                mRecyclerView.getLayoutManager().getOrientation());
////        recyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
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
                    mProgressDialog.cancel();
                    if (queryDocumentSnapshots.getDocuments().size() == 0) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWaitForResultTournament.setText(R.string.result_not_found);
                            }
                        });
                    } else {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        mAdapter = new TournamentRecyclerViewAdapter(getContext(), documents);
                        mRecyclerView.setAdapter(mAdapter);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWaitForResultTournament.setText("");//mWaitForResultTournament.setVisibility(View.INVISIBLE);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.not_valid, Toast.LENGTH_LONG).show();
        }
    }
}
