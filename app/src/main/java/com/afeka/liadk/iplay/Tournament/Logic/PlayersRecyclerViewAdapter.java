package com.afeka.liadk.iplay.Tournament.Logic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afeka.liadk.iplay.FireBaseConst;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.UserProfile.Logic.UserData;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by liadk
 */
public class PlayersRecyclerViewAdapter extends RecyclerView.Adapter<PlayersRecyclerViewAdapter.ViewHolder> implements FireBaseConst {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private boolean mPermission;
    private CollectionReference mCollectionReference;

    public PlayersRecyclerViewAdapter(Context context, ArrayList<String> data, boolean mPermission) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mPermission = mPermission;
        mCollectionReference = FirebaseFirestore.getInstance().collection(USERS);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.user_recyclerview_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        String username = mData.get(position);
        viewHolder.mUsername.setText((1 + position) + ". " + username);
        if (mPermission) {
            mCollectionReference.document(username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    Glide.with(mInflater.getContext()).load(userData.getmUriImage()).into(viewHolder.mCircleImageView);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsername;
        private CircleImageView mCircleImageView;

        public ViewHolder(View view) {
            super(view);
            mUsername = view.findViewById(R.id.username_in_tournament);
            mCircleImageView = view.findViewById(R.id.user_in_tournament_proflie_image);
        }
    }
}
