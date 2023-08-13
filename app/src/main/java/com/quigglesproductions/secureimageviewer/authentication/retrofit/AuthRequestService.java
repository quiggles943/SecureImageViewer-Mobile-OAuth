package com.quigglesproductions.secureimageviewer.authentication.retrofit;

import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.UserInfoResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalTokenRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalTwoFactorRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.UserAuthorizationRequest;
import com.quigglesproductions.secureimageviewer.models.DeviceStatus;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationResponseModel;

import java.util.HashMap;
import java.util.concurrent.Executor;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AuthRequestService {
    @Multipart
    @POST("/v1/oauth2/token")
    Call<TokenResponse> doGetAuthToken(@PartMap HashMap<String, RequestBody> request);
    @Multipart
    @POST("/v1/oauth2/token")
    Call<TokenResponse> doRefreshToken(@PartMap HashMap<String, RequestBody> request);
    @GET("/v1/oauth2/userinfo")
    Call<UserInfoResponse> doGetUserInfo(@Header("Authorization") String authHeader);
    @POST()
    Call<DeviceRegistrationResponseModel> doRegisterDevice(@Url String url, @Body DeviceRegistrationModel deviceRegistrationModel);

    @GET()
    Call<DeviceStatus> doGetDeviceStatus(@Url String url,@Query("deviceId") String deviceId);
    @POST("/internal/v1/oauth2/authorize")
    Call<InternalAuthResponse> doAuthorizeUser(@Body UserAuthorizationRequest userAuthorizationRequest);
    @POST("/internal/v1/oauth2/authenticate")
    Call<InternalAuthResponse> doAuthenticateCode(@Body InternalTwoFactorRequest twoFactorRequest);
    @POST("/internal/v1/oauth2/token")
    Call<InternalAuthResponse> doRetrieveToken(@Body InternalTokenRequest tokenRequest);
}
