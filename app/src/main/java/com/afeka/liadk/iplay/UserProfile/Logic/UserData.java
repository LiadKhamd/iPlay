package com.afeka.liadk.iplay.UserProfile.Logic;

import android.net.Uri;

import com.afeka.liadk.iplay.Tournament.Logic.UserTournamentRegister;

/*
 * Created by liadk
 */
public class UserData {

    private String mUserUID;
    private UserTournamentRegister mEvent;
    private Uri mUriImage;

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

    public Uri getmUriImage() {
        return mUriImage;
    }

    public void setmEvent(UserTournamentRegister mEvent) {
        this.mEvent = mEvent;
    }

    public void setmUriImage(Uri mUriImage) {
        this.mUriImage = mUriImage;
    }
}
