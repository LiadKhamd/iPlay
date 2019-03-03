package com.afeka.liadk.iplay.Tournament.Logic;

/**
 * Created by liadk
 */
public class UserTournamentRegister {
    private String mEventKey, mCity, mSport;

    public UserTournamentRegister() {

    }

    public UserTournamentRegister(String mCity, String mSport, String mEventKey) {
        this.mCity = mCity;
        this.mSport = mSport;
        this.mEventKey = mEventKey;
    }

    public String getmEventKey() {
        return mEventKey;
    }

    public String getmCity() {
        return mCity;
    }

    public String getmSport() {
        return mSport;
    }

    public void setmEventKey(String mEventKey) {
        this.mEventKey = mEventKey;
    }

    public void setmCity(String mCity) {
        this.mCity = mCity;
    }

    public void setmSport(String mSport) {
        this.mSport = mSport;
    }
}
