package com.afeka.liadk.iplay.UserProfile.Logic;

import android.net.Uri;

import com.afeka.liadk.iplay.Tournament.Logic.UserTournamentRegister;

/*
 * Created by liadk
 */
public class UserData {

    private String mUserUID;
    private UserTournamentRegister mEvent;
    private String mUriImage;

    public UserData() {

    }

    public UserData(String mUserUID) {
        this.mUserUID = mUserUID;
        mEvent = null;
        mUriImage = null;
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

    public String getmUriImage() {
        return mUriImage;
    }

    public void setmEvent(UserTournamentRegister mEvent) {
        this.mEvent = mEvent;
    }

    public void setmUriImage(String mUriImage) {
        this.mUriImage = mUriImage;
    }
}
