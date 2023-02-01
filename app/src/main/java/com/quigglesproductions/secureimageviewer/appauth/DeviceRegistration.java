package com.quigglesproductions.secureimageviewer.appauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.common.BuildConfig;
import com.quigglesproductions.secureimageviewer.apprequest.requests.DeviceRegistrationRequest;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

public class DeviceRegistration {
    private Context rootContext;
    private RegistrationId registrationId;
    SharedPreferences registrationPref;
    public DeviceRegistration(Context context){
        rootContext = context.getApplicationContext();
        loadRegistrationId();
    }

    private SharedPreferences getRegistrationPref(){
        if(registrationPref == null) {
            registrationPref = rootContext.getSharedPreferences("com.secureimageviewer.registration", Context.MODE_PRIVATE);
        }
        return registrationPref;
    }

    public RegistrationId getRegistrationID() {
        return this.registrationId;
    }

    public void retrieveRegistrationId(Context context,DeviceRegistrationRetrievalCallback callback){
        if(isRegistrationIdSet())
            callback.DeviceRegistrationRetrieved(registrationId,null);
        else{
            /*String deviceId = Settings.Secure.getString(rootContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            if(BuildConfig.BUILD_TYPE.contentEquals("debug"))
                deviceId = deviceId+"-debug";*/
            RegistrationId registrationId = new RegistrationId();
            registrationId.setDeviceId(getDeviceId());
            String deviceName = Settings.Secure.getString(rootContext.getContentResolver(), "bluetooth_name");
            registrationId.setDeviceName(deviceName);
            registerDevice(context,registrationId, new DeviceRegistrationRequest.DeviceRegistrationCallback() {
                @Override
                public void deviceRegistered(RegistrationId registrationId, Exception ex) {
                    if(registrationId != null)
                        setDeviceAuthenticated(true);
                    callback.DeviceRegistrationRetrieved(registrationId,ex);
                }
            });
        }
    }

    private RegistrationId loadRegistrationId(){
        String regIDJson = getRegistrationPref().getString("registrationId",null);
        RegistrationId currentRegId = RegistrationId.fromJsonString(regIDJson);
        registrationId = currentRegId;
        return currentRegId;
    }

    public boolean getDeviceAuthenticated(){
        return getRegistrationPref().getBoolean("isAuthenticated",false);
    }

    public void setDeviceAuthenticated(boolean isAuthenticated){
        SharedPreferences.Editor editor = getRegistrationPref().edit();
        editor.putBoolean("isAuthenticated",isAuthenticated);
        editor.apply();
    }

    public boolean isRegistrationIdSet() {
        if(registrationId == null)
            return false;
        if(registrationId.getRegistrationId() == null)
            return false;

        return true;
    }

    public void registerDevice(Context context,RegistrationId registrationId,DeviceRegistrationRequest.DeviceRegistrationCallback callback){

        AuthManager.getInstance().performActionWithFreshTokens(context,new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                DeviceRegistrationRequest request = new DeviceRegistrationRequest();
                request.registerDevice(context,registrationId, new DeviceRegistrationRequest.DeviceRegistrationCallback() {
                    @Override
                    public void deviceRegistered(RegistrationId registrationId, Exception ex) {
                        if(registrationId != null && ex == null) {
                            setRegistrationId(registrationId);
                            SharedPreferences.Editor editor = getRegistrationPref().edit();
                            editor.putString("registrationId", registrationId.toJsonString());
                            editor.apply();
                        }
                        callback.deviceRegistered(registrationId,ex);
                    }
                });
            }
        });
    }

    private void setRegistrationId(RegistrationId registrationId){
        this.registrationId = registrationId;
    }

    public void UpdateRegistrationId(RegistrationId registrationId) {
        setRegistrationId(registrationId);
        SharedPreferences.Editor editor = getRegistrationPref().edit();
        String jsonString = registrationId.toJsonString();
        editor.putString("registrationId", jsonString);
        editor.apply();
    }

    public String getDeviceId(){
        String deviceId = Settings.Secure.getString(rootContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(BuildConfig.BUILD_TYPE.contentEquals("debug"))
            deviceId = deviceId+"-debug";
        return deviceId;
    }


    public interface DeviceRegistrationRetrievalCallback{
        public void DeviceRegistrationRetrieved(RegistrationId registrationId, Exception ex);
    }
}
