package com.afeka.liadk.iplay.Tournament;

import java.util.ArrayList;

/**
 * Created by liadk
 */
public class TournamentInfo {
    private String mCity, mPlace, mSport, mCode;
    private boolean mPrivate;
    private String mCreatorUsername;
    private ArrayList<String> mParticipantsUsersnames;
    private int mMaxParticipants;
    private long mTime;

    public TournamentInfo() {

    }

    public TournamentInfo(String mCity, String mPlace, String mSport, long mTime, int mMaxParticipants, boolean mPrivate, String mCode, String mCreatorUsername) {
        this.mCity = mCity;
        this.mPlace = mPlace;
        this.mSport = mSport;
        this.mCode = mCode;
        this.mPrivate = mPrivate;
        this.mTime = mTime;
        this.mCreatorUsername = mCreatorUsername;
        this.mMaxParticipants = mMaxParticipants;
        mParticipantsUsersnames = new ArrayList<>();
        mParticipantsUsersnames.add(mCreatorUsername);
    }

    public String getmCity() {
        return mCity;
    }

    public String getmPlace() {
        return mPlace;
    }

    public String getmSport() {
        return mSport;
    }

    public String getmCode() {
        return mCode;
    }

    public boolean ismPrivate() {
        return mPrivate;
    }

    public String getmCreatorUsername() {
        return mCreatorUsername;
    }

    public ArrayList<String> getmParticipantsUsersnames() {
        return mParticipantsUsersnames;
    }

    public int getmMaxParticipants() {
        return mMaxParticipants;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmCity(String mCity) {
        this.mCity = mCity;
    }

    public void setmPlace(String mPlace) {
        this.mPlace = mPlace;
    }

    public void setmSport(String mSport) {
        this.mSport = mSport;
    }

    public void setmCode(String mCode) {
        this.mCode = mCode;
    }

    public void setmPrivate(boolean mPrivate) {
        this.mPrivate = mPrivate;
    }

    public void setmCreatorUsername(String mCreatorUsername) {
        this.mCreatorUsername = mCreatorUsername;
    }

    public void setmParticipantsUsersnames(ArrayList<String> mParticipantsUsersnames) {
        this.mParticipantsUsersnames = mParticipantsUsersnames;
    }

    public void setmMaxParticipants(int mMaxParticipants) {
        this.mMaxParticipants = mMaxParticipants;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }
}
