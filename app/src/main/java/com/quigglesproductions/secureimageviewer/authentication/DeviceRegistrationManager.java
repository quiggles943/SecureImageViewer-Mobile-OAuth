package com.quigglesproductions.secureimageviewer.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;


import com.google.android.exoplayer2.common.BuildConfig;
import com.quigglesproductions.secureimageviewer.appauth.DeviceRegistration;
import com.quigglesproductions.secureimageviewer.apprequest.requests.DeviceRegistrationRequest;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationResponseModel;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Module
@InstallIn(SingletonComponent.class)
public class DeviceRegistrationManager {

    private Context context;
    private RegistrationId registrationId;
    SharedPreferences registrationPref;
    Lazy<AuthenticationManager> authenticationManagerLazy;
    private Settings.Secure secureSettings;

    DeviceRegistrationManager(){

    }
    @Inject
    public DeviceRegistrationManager(@ApplicationContext Context context, Lazy<AuthenticationManager> authenticationManagerLazy){
        this.context = context;
        this.authenticationManagerLazy = authenticationManagerLazy;
    }

    SharedPreferences getRegistrationPref(){
        if(registrationPref == null) {
            registrationPref = getContext().getSharedPreferences("com.secureimageviewer.registration", Context.MODE_PRIVATE);
        }
        return registrationPref;
    }

    public RegistrationId getRegistrationID() {
        if(registrationId == null)
            loadRegistrationId();
        if(registrationId == null){
            registrationId = new RegistrationId();
            registrationId.setDeviceId(getDeviceId());
            registrationId.setDeviceName(getDeviceName());
            updateRegistrationId(registrationId);
        }
        return this.registrationId;
    }

    public void retrieveRegistrationId(Context context, DeviceRegistration.DeviceRegistrationRetrievalCallback callback){
        if(isRegistrationIdSet())
            callback.DeviceRegistrationRetrieved(registrationId,null);
        else{
            RegistrationId registrationId = new RegistrationId();
            registrationId.setDeviceId(getDeviceId());
            String deviceName = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
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

    Context getContext(){
        return context;
    }

    RegistrationId loadRegistrationId(){
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
        AuthenticationManager authenticationManager = authenticationManagerLazy.get();
        DeviceRegistrationModel sendModel = new DeviceRegistrationModel();
        sendModel.deviceName = registrationId.getDeviceName();
        sendModel.deviceId = registrationId.getDeviceId();
        authenticationManager.registerDevice(sendModel,new Callback<DeviceRegistrationResponseModel>() {
            @Override
            public void onResponse(Call<DeviceRegistrationResponseModel> call, Response<DeviceRegistrationResponseModel> response) {
                if(response.isSuccessful()){
                    DeviceRegistrationResponseModel responseModel = response.body();
                    registrationId.setRegistrationId(responseModel.Id);
                    setRegistrationId(registrationId);
                    SharedPreferences.Editor editor = getRegistrationPref().edit();
                    editor.putString("registrationId", registrationId.toJsonString());
                    editor.apply();
                    callback.deviceRegistered(registrationId,null);
                }
                else
                    callback.deviceRegistered(null,null);
            }

            @Override
            public void onFailure(Call<DeviceRegistrationResponseModel> call, Throwable t) {
                callback.deviceRegistered(null,new RetrofitException(t));
            }
        });
    }

    private void setRegistrationId(RegistrationId registrationId){
        this.registrationId = registrationId;
    }

    public void updateRegistrationId(RegistrationId registrationId) {
        setRegistrationId(registrationId);
        SharedPreferences.Editor editor = getRegistrationPref().edit();
        String jsonString = registrationId.toJsonString();
        editor.putString("registrationId", jsonString);
        editor.apply();
    }

    public String getDeviceId(){
        String deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if(BuildConfig.BUILD_TYPE.contentEquals("debug"))
            deviceId = deviceId+"-debug";
        return deviceId;
    }

    public String getDeviceName(){
        String deviceName = Settings.Secure.getString(getContext().getContentResolver(), "bluetooth_name");
        return deviceName;
    }


    public interface DeviceRegistrationRetrievalCallback{
        public void DeviceRegistrationRetrieved(RegistrationId registrationId, Exception ex);
    }
}
