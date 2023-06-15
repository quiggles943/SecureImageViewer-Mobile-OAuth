package com.quigglesproductions.secureimageviewer.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRetrievalRequest;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.models.DeviceStatus;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationResponseModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationManagerTest {
    @Mock
    Context context;
    AuthenticationManager authenticationManager;
    @Mock
    TokenManager tokenManager;
    @Mock
    AuthRequestService authRequestService;
    @Mock
    DeviceRegistrationManager deviceRegistrationManager;

    @Before
    public void setup(){
        authenticationManager = Mockito.spy(new AuthenticationManager(context));
        authenticationManager.tokenManager = tokenManager;
        authenticationManager.authRequestService = authRequestService;
        authenticationManager.deviceRegistrationManager = deviceRegistrationManager;
        //Mockito.doReturn(tokenManager).when(authenticationManager).getTokenManager();
    }

    @Test
    public void generateTokenRetrievalRequestTest(){
        Mockito.doReturn("TestId").when(authenticationManager).getClientId();
        Mockito.doReturn("TestSecret").when(authenticationManager).getClientSecret();
        TokenRetrievalRequest actualRequest = authenticationManager.generateTokenRetrievalRequest();
        Assert.assertEquals("TestId",actualRequest.clientId);
        Assert.assertEquals("TestSecret",actualRequest.clientSecret);
        Assert.assertEquals("read, write",actualRequest.scope);
    }

    @Test
    public void generateTokenRefreshRequestTest(){
        Mockito.doReturn("TestId").when(authenticationManager).getClientId();
        Mockito.when(tokenManager.getRefreshToken()).thenReturn("TestRefreshToken");
        TokenRefreshRequest actualRequest = authenticationManager.generateTokenRefreshRequest();
        Assert.assertEquals("TestId",actualRequest.clientId);
        Assert.assertEquals("TestRefreshToken",actualRequest.refreshToken);
    }

    @Test
    public void updateAuthenticationStateTest(){
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.access_token = "TestAccessToken";
        tokenResponse.refresh_token = "TestAccessToken";
        AuthenticationState authState = new AuthenticationState.Builder().fromTokenResponse(tokenResponse).build();
        authenticationManager.updateAuthenticationState(authState);
        verify(tokenManager).setAuthenticaionState(authState);
    }

    @Test
    public void getRequestServiceTest(){
        Assert.assertEquals(authRequestService,authenticationManager.getRequestService());
    }

    @Test
    public void registerDeviceTest(){
        DeviceRegistrationModel registrationModel = new DeviceRegistrationModel();
        registrationModel.deviceName = "Test";
        Mockito.doReturn(authRequestService).when(authenticationManager).getRequestService();
        Call call = Mockito.mock(Call.class);
        when(authRequestService.doRegisterDevice(anyString(),any())).thenReturn(call);
        Callback<DeviceRegistrationResponseModel> callback = Mockito.mock(Callback.class);
        authenticationManager.registerDevice(registrationModel, callback);
        verify(authRequestService).doRegisterDevice("https://quigleyserver.ddns.net:14500/api/v1/device/register",registrationModel);
    }

    @Test
    public void getDeviceStatusTest(){
        DeviceRegistrationModel registrationModel = new DeviceRegistrationModel();
        registrationModel.deviceName = "Test";
        Mockito.doReturn(authRequestService).when(authenticationManager).getRequestService();
        Mockito.doReturn("TestId").when(deviceRegistrationManager).getDeviceId();
        Call call = Mockito.mock(Call.class);
        when(authRequestService.doGetDeviceStatus(anyString(),anyString())).thenReturn(call);
        Callback<DeviceStatus> callback = Mockito.mock(Callback.class);
        authenticationManager.getDeviceStatus(callback);
        verify(authRequestService).doGetDeviceStatus(anyString(),anyString());
    }

    @Test
    public void getDeviceRegistrationManagerTest(){
        Assert.assertEquals(deviceRegistrationManager,authenticationManager.getDeviceRegistrationManager());
    }
}
