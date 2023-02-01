package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;

public interface ISecureDataSource {


    AuthManager getAuthorization();
}
