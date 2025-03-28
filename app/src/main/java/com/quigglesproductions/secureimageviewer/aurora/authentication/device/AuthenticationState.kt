package com.quigglesproductions.secureimageviewer.aurora.authentication.device

enum class AuthenticationState(val title:String){
    VERIFYING("Verifying"),
    AUTHENTICATED("Authenticated"),
    UNABLE_TO_AUTHENTICATE("Unable to Authenticate"),
    NOT_AUTHENTICATED("Not Authenticated")

}