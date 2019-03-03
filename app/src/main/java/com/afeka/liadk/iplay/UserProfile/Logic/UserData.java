package com.afeka.liadk.iplay.UserProfile.Logic;

import com.afeka.liadk.iplay.Tournament.Logic.UserTournamentRegister;

/*
 * Created by liadk
 */
public class UserData {

    private String mUserUID;
    private UserTournamentRegister mEvent;

    public UserData() {

    }

    public UserData(String mUserUID) {
        this.mUserUID = mUserUID;
        mEvent = null;
    }

    public String getmUserUID() {
        return mUserUID;
    }

    public void setmUserUID(String mUserUID) {
        this.mUserUID = mUserUID;
    }

    public UserTournamentRegister getmEvent() {
        return mEvent;
    }

    public void setmEvent(String mCity, String mSport, String mEventKey) {
        this.mEvent = new UserTournamentRegister(mCity, mSport, mEventKey);
    }
}
