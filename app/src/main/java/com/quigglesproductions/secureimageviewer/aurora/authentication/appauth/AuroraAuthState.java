package com.quigglesproductions.secureimageviewer.aurora.authentication.appauth;

import static net.openid.appauth.Preconditions.checkNotEmpty;
import static net.openid.appauth.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.IdToken;
import net.openid.appauth.RegistrationResponse;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AuroraAuthState extends AuthState {
    private static final String KEY_CONFIG = "config";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_REFRESH_TOKEN_EXPIRATION = "refreshTokenExpiration";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_LAST_AUTHORIZATION_RESPONSE = "lastAuthorizationResponse";
    private static final String KEY_LAST_TOKEN_RESPONSE = "mLastTokenResponse";
    private static final String KEY_AUTHORIZATION_EXCEPTION = "mAuthorizationException";
    private static final String KEY_LAST_REGISTRATION_RESPONSE = "lastRegistrationResponse";
    private Long mRefreshTokenExpirationTime;
    private AuthState authState;
    public AuroraAuthState(@NonNull AuthorizationServiceConfiguration config){
        this.authState = new AuthState(config);
    }
    public AuroraAuthState(AuthState authState){
        this.authState = authState;
    }
    public AuroraAuthState(AuroraAuthState viewerAuthState){
        this.authState = viewerAuthState.authState;
    }

    public AuroraAuthState() {
        this.authState = new AuthState();
    }

    public Long getRefreshTokenExpirationTime(){
        if(mRefreshTokenExpirationTime == null)
            return null;
        else
            return this.mRefreshTokenExpirationTime;
    }
    public void setRefreshTokenExpirationTime(@Nullable Long expiresIn){
        if(expiresIn != null) {
            mRefreshTokenExpirationTime = System.currentTimeMillis()
                    + TimeUnit.SECONDS.toMillis(expiresIn);
        }
    }
    public void setRefreshTokenExpirationTimeMillis(@Nullable Long expiresMillis){
        mRefreshTokenExpirationTime = expiresMillis;

    }

    public Date getRefreshTokenExpirationDate(){
        if(mRefreshTokenExpirationTime == null)
            return null;
        return new Date(mRefreshTokenExpirationTime);
    }

    public static AuroraAuthState jsonDeserialize(@NonNull String jsonStr) throws JSONException {
        checkNotEmpty(jsonStr, "jsonStr cannot be null or empty");
        return jsonDeserialize(new JSONObject(jsonStr));
    }

    @Nullable
    @Override
    public String getRefreshToken() {
        return authState.getRefreshToken();
    }

    @Nullable
    @Override
    public String getScope() {
        return authState.getScope();
    }

    @Nullable
    @Override
    public Set<String> getScopeSet() {
        return authState.getScopeSet();
    }

    @Nullable
    @Override
    public AuthorizationResponse getLastAuthorizationResponse() {
        return authState.getLastAuthorizationResponse();
    }

    @Nullable
    @Override
    public TokenResponse getLastTokenResponse() {
        return authState.getLastTokenResponse();
    }

    @Nullable
    @Override
    public RegistrationResponse getLastRegistrationResponse() {
        return authState.getLastRegistrationResponse();
    }

    @Nullable
    @Override
    public AuthorizationServiceConfiguration getAuthorizationServiceConfiguration() {
        return authState.getAuthorizationServiceConfiguration();
    }

    @Nullable
    @Override
    public String getAccessToken() {
        return authState.getAccessToken();
    }

    @Nullable
    @Override
    public Long getAccessTokenExpirationTime() {
        return authState.getAccessTokenExpirationTime();
    }

    @Nullable
    @Override
    public String getIdToken() {
        return authState.getIdToken();
    }

    @Nullable
    @Override
    public IdToken getParsedIdToken() {
        return authState.getParsedIdToken();
    }

    @Override
    public String getClientSecret() {
        return authState.getClientSecret();
    }

    @Nullable
    @Override
    public Long getClientSecretExpirationTime() {
        return authState.getClientSecretExpirationTime();
    }

    @Override
    public boolean isAuthorized() {
        return authState.isAuthorized();
    }

    @Nullable
    @Override
    public AuthorizationException getAuthorizationException() {
        return authState.getAuthorizationException();
    }

    @Override
    public boolean getNeedsTokenRefresh() {
        return authState.getNeedsTokenRefresh();
    }

    @Override
    public void setNeedsTokenRefresh(boolean needsTokenRefresh) {
        authState.setNeedsTokenRefresh(needsTokenRefresh);
    }

    @Override
    public boolean hasClientSecretExpired() {
        return authState.hasClientSecretExpired();
    }

    @Override
    public void update(@Nullable AuthorizationResponse authResponse, @Nullable AuthorizationException authException) {
        authState.update(authResponse, authException);
    }

    @Override
    public void update(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException authException) {
        authState.update(tokenResponse, authException);
    }

    @Override
    public void update(@Nullable RegistrationResponse regResponse) {
        authState.update(regResponse);
    }

    @Override
    public void performActionWithFreshTokens(@NonNull AuthorizationService service, @NonNull AuthStateAction action) {
        authState.performActionWithFreshTokens(service, action);
    }

    @Override
    public void performActionWithFreshTokens(@NonNull AuthorizationService service, @NonNull ClientAuthentication clientAuth, @NonNull AuthStateAction action) {
        authState.performActionWithFreshTokens(service, clientAuth, action);
    }

    @Override
    public void performActionWithFreshTokens(@NonNull AuthorizationService service, @NonNull Map<String, String> refreshTokenAdditionalParams, @NonNull AuthStateAction action) {
        authState.performActionWithFreshTokens(service, refreshTokenAdditionalParams, action);
    }

    @Override
    public void performActionWithFreshTokens(@NonNull AuthorizationService service, @NonNull ClientAuthentication clientAuth, @NonNull Map<String, String> refreshTokenAdditionalParams, @NonNull AuthStateAction action) {
        authState.performActionWithFreshTokens(service, clientAuth, refreshTokenAdditionalParams, action);
    }

    @NonNull
    @Override
    public TokenRequest createTokenRefreshRequest() {
        return authState.createTokenRefreshRequest();
    }

    @NonNull
    @Override
    public TokenRequest createTokenRefreshRequest(@NonNull Map<String, String> additionalParameters) {
        return authState.createTokenRefreshRequest(additionalParameters);
    }

    @Override
    public JSONObject jsonSerialize() {
        JSONObject json = new JSONObject();
        AuroraJsonUtil.putIfNotNull(json, KEY_REFRESH_TOKEN, getRefreshToken());
        AuroraJsonUtil.putIfNotNull(json,KEY_REFRESH_TOKEN_EXPIRATION,getRefreshTokenExpirationTime());
        AuroraJsonUtil.putIfNotNull(json, KEY_SCOPE, getScope());

        if (getAuthorizationServiceConfiguration() != null) {
            AuroraJsonUtil.put(json, KEY_CONFIG, getAuthorizationServiceConfiguration().toJson());
        }

        if (getAuthorizationException() != null) {
            AuroraJsonUtil.put(json, KEY_AUTHORIZATION_EXCEPTION, getAuthorizationException().toJson());
        }

        if (getLastAuthorizationResponse() != null) {
            AuroraJsonUtil.put(
                    json,
                    KEY_LAST_AUTHORIZATION_RESPONSE,
                    getLastAuthorizationResponse().jsonSerialize());
        }

        if (getLastTokenResponse() != null) {
            AuroraJsonUtil.put(
                    json,
                    KEY_LAST_TOKEN_RESPONSE,
                    getLastTokenResponse().jsonSerialize());
        }

        if (getLastRegistrationResponse() != null) {
            AuroraJsonUtil.put(
                    json,
                    KEY_LAST_REGISTRATION_RESPONSE,
                    getLastRegistrationResponse().jsonSerialize());
        }


        return json;
    }

    @Override
    public String jsonSerializeString() {
        return jsonSerialize().toString();
    }

    @Override
    public ClientAuthentication getClientAuthentication() throws ClientAuthentication.UnsupportedAuthenticationMethod {
        return authState.getClientAuthentication();
    }

    /**
     * Reads an authorization state instance from a JSON string representation produced by
     * {@link #jsonSerialize()}.
     * @throws JSONException if the provided JSON does not match the expected structure.
     */
    public static AuroraAuthState jsonDeserialize(@NonNull JSONObject json) throws JSONException {
        checkNotNull(json, "json cannot be null");
        AuroraAuthState authState = new AuroraAuthState();
        AuthState state = AuthState.jsonDeserialize(json);
        authState.authState = state;
        if(json.optLong(KEY_REFRESH_TOKEN_EXPIRATION,-1) != -1){
            authState.setRefreshTokenExpirationTimeMillis(json.optLong(KEY_REFRESH_TOKEN_EXPIRATION,-1));
        }
        return authState;
    }

}
