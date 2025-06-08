package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.models.LoginModel;

public class SecurityManager {
    public static final int LOGIN = 486584;
    public static final String LoginObject = "login";
    private static final int RESULT_NO_BIOMETRIC = 128;
    private static final int RESULT_AUTH_ERROR = 111;
    public static final String ERROR_RESULT = "SecurityManager.Error";
    private static final String userPreferences = "com.secureimageviewer.preference.security.user";
    private static SecurityManager singleton;
    private Context rootContext;
    private LoginModel login;

    public static SecurityManager getInstance(){
        if(singleton == null)
            singleton = new SecurityManager();
        return singleton;
    }
    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
    }

    public void setLogin(LoginModel login){
        this.login = login;
    }
}
