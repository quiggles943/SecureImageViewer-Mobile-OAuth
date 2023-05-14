package com.quigglesproductions.secureimageviewer.dagger.hilt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

public class HttpClientQualifier {
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface AuthServiceClient{}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface RequestServiceClient{}
}
