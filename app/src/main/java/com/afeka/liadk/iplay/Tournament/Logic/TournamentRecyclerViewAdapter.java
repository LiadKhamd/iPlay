package com.afeka.liadk.iplay.Tournament.Logic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afeka.liadk.iplay.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.List;

/**
 * Created by liadk
 */
public class TournamentRecyclerViewAdapter extends RecyclerView.Adapter<TournamentRecyclerViewAdapter.ViewHolder> {

    private List<DocumentSnapshot> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public TournamentRecyclerViewAdapter(Context context, List<DocumentSnapshot> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.tournament_recyclerview_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        TournamentInfo tournamentInfo = mData.get(position).toObject(TournamentInfo.class);
        viewHolder.mPlace.setText(tournamentInfo.getmPlace());
        Date date = new Date(tournamentInfo.getmTime());
        if (date.getMinutes() < 10 || date.getMinutes() == 0)
            viewHolder.mTime.setText(date.getHours() + ":" + date.getMinutes() + "0");
        else
            viewHolder.mTime.setText(date.getHours() + ":" + date.getMinutes());
        viewHolder.mPlayers.setText(tournamentInfo.getPlayers() + "/" + tournamentInfo.getmMaxParticipants());
        if (tournamentInfo.ismPrivate()) {
            viewHolder.mPrivate.setText(R.string.is_private);
            viewHolder.mPrivate.setTextColor(mInflater.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            viewHolder.mPrivate.setText(R.string.is_public);
            viewHolder.mPrivate.setTextColor(mInflater.getContext().getResources().getColor(android.R.color.holo_green_dark));
        }

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mPlace, mTime, mPlayers, mPrivate;

        ViewHolder(View itemView) {
            super(itemView);
            mPlace = itemView.findViewById(R.id.place_of_tournament);
            mTime = itemView.findViewById(R.id.time_of_tournament);
            mPrivate = itemView.findViewById(R.id.tournament_is_private);
            mPlayers = itemView.findViewById(R.id.current_tournament_players);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
