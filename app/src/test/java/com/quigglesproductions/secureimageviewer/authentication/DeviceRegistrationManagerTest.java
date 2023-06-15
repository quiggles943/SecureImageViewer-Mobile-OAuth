package com.quigglesproductions.secureimageviewer.authentication;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import dagger.Lazy;

@RunWith(MockitoJUnitRunner.class)
public class DeviceRegistrationManagerTest {

    @Mock
    Context context;
    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor sharedPreferencesEditor;
    @Spy
    DeviceRegistrationManager registrationManager;

    String defaultJson = "{\"deviceId\":\"c4afbe29673ae434\",\"deviceName\":\"sdk_gphone64_x86_64\",\"nextCheckIn\":\"2023-07-14T12:02:39.8876484\"}";

    @Before
    public void setup(){
        Mockito.when(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor);
        //doReturn(context).when(registrationManager).getContext();
        doReturn(sharedPreferences).when(registrationManager).getRegistrationPref();
        //registrationManager = spy(DeviceRegistrationManager.class);
        //registrationManager = new DeviceRegistrationManager(context,authenticationManager);
    }

    @Test
    public void getRegistrationPreferencesTest(){
        SharedPreferences actualPreferences = registrationManager.getRegistrationPref();
        Assert.assertEquals(sharedPreferences,actualPreferences);
    }
    @Test
    public void getRegistrationIdExistingTest(){
        Mockito.when(sharedPreferences.getString("registrationId",null)).thenReturn(defaultJson);
        RegistrationId actualId = registrationManager.getRegistrationID();
        Assert.assertEquals("c4afbe29673ae434",actualId.getDeviceId());
        Assert.assertEquals("sdk_gphone64_x86_64",actualId.getDeviceName());
        Assert.assertEquals(LocalDateTime.parse("2023-07-14T12:02:39.8876484"),actualId.getNextCheckIn());
    }
    @Test
    public void getRegistrationIdNotExistingTest(){
        Mockito.when(sharedPreferences.getString("registrationId",null)).thenReturn(null);
        Mockito.doReturn("Test").when(registrationManager).getDeviceId();
        Mockito.doReturn("TestName").when(registrationManager).getDeviceName();
        RegistrationId actualId = registrationManager.getRegistrationID();
        Assert.assertEquals("Test",actualId.getDeviceId());
        Assert.assertEquals("TestName",actualId.getDeviceName());
        Assert.assertNull(actualId.getNextCheckIn());
    }
    @Test
    public void loadRegistrationIdTest(){
        Mockito.when(sharedPreferences.getString("registrationId",null)).thenReturn(defaultJson);
        RegistrationId actualId = registrationManager.loadRegistrationId();
        Assert.assertEquals("c4afbe29673ae434",actualId.getDeviceId());
        Assert.assertEquals("sdk_gphone64_x86_64",actualId.getDeviceName());
        Assert.assertEquals(LocalDateTime.parse("2023-07-14T12:02:39.8876484"),actualId.getNextCheckIn());
    }
    @Test
    public void getDeviceAuthenticatedTest(){
        Mockito.when(sharedPreferences.getBoolean("isAuthenticated",false)).thenReturn(true);
        Assert.assertTrue(registrationManager.getDeviceAuthenticated());
    }

    @Test
    public void setDeviceAuthenticatedTest(){
        registrationManager.setDeviceAuthenticated(true);
        verify(sharedPreferencesEditor,times(1)).putBoolean("isAuthenticated",true);
        verify(sharedPreferencesEditor,times(1)).apply();
    }

    @Test
    public void updateRegistrationIdTest(){
        registrationManager.updateRegistrationId(RegistrationId.fromJsonString(defaultJson));
        verify(sharedPreferencesEditor,times(1)).putString("registrationId",defaultJson);
        verify(sharedPreferencesEditor,times(1)).apply();
    }
}
