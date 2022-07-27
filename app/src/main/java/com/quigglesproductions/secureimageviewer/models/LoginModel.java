package com.quigglesproductions.secureimageviewer.models;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginModel implements Parcelable {
    private boolean loggedIn;
    private String userId;
    private boolean authenticated;

    public LoginModel(){

    }

    protected LoginModel(Parcel in) {
        loggedIn = in.readByte() != 0;
        userId = in.readString();
        authenticated = in.readByte() != 0;
    }

    public static final Creator<LoginModel> CREATOR = new Creator<LoginModel>() {
        @Override
        public LoginModel createFromParcel(Parcel in) {
            return new LoginModel(in);
        }

        @Override
        public LoginModel[] newArray(int size) {
            return new LoginModel[size];
        }
    };

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUserId() {
        return userId;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (loggedIn ? 1 : 0));
        dest.writeString(userId);
        dest.writeByte((byte) (authenticated ? 1 : 0));
    }
}
