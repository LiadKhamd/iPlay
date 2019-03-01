package com.afeka.liadk.iplay.UserProfile;

/*
 * Created by liadk
 */
public class UserData {

    private String mUserUID, mEvent;

    public UserData() {

    }

    public UserData(String mUserUID) {
        this.mUserUID = mUserUID;
        mEvent = null;
    }

    public String getmUserUID() {
        return mUserUID;
    }

    public String getmEvent() {
        return mEvent;
    }

    public void setmUserUID(String mUserUID) {
        this.mUserUID = mUserUID;
    }

    public void setmEvent(String mEvent) {
        this.mEvent = mEvent;
    }
}
