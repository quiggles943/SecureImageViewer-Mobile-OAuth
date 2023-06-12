package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.authentication.IAuthenticationLayer;

public interface ISecureDataSource {


    IAuthenticationLayer getAuthorization();
}
