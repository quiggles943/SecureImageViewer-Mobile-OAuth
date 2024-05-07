package com.quigglesproductions.secureimageviewer.datasource;

import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;

public interface ISecureDataSource {


    AuroraAuthenticationManager getAuthorization();
}
