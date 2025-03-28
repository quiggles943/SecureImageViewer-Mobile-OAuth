package com.quigglesproductions.secureimageviewer.aurora.authentication.device

class DeviceAuthenticationResult(val authenticationState: AuthenticationState, val exception: Exception?) {
    constructor(authenticationState: AuthenticationState) : this(authenticationState,null)
}