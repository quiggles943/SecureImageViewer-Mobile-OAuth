package com.quigglesproductions.secureimageviewer.authentication.pogo;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class UserInfoResponse {
    @SerializedName("Id")
    public String Id;
    @SerializedName("UserId")
    public String UserId;
    @SerializedName("UserName")
    public String UserName;
    @SerializedName("FirstName")
    public String FirstName;
    @SerializedName("LastName")
    public String LastName;
    @SerializedName("DateOfBirth")
    public LocalDateTime DateOfBirth;
    @SerializedName("EmailAddress")
    public String EmailAddress;
    @SerializedName("EmailVerified")
    public boolean EmailVerified;
    @SerializedName("PhoneNumber")
    public String PhoneNumber;
    @SerializedName("TwoFactorEnabled")
    public boolean TwoFactorEnabled;
    @SerializedName("PhoneNumberVerified")
    public boolean PhoneNumberVerified;
}
