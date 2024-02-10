package com.quigglesproductions.secureimageviewer.datasource;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.IAuthenticationLayer;

public interface ISecureDataSource {


    AuroraAuthenticationManager getAuthorization();
}
