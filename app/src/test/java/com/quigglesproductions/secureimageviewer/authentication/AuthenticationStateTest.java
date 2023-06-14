package com.quigglesproductions.secureimageviewer.authentication;

import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;

import org.checkerframework.checker.units.qual.C;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationStateTest {
    String authStateJson = "{\"accessToken\":\"TestAccessToken\",\"refreshToken\":\"TestRefreshToken\",\"scopes\":[\"TestScope1\",\"TestScope2\"],\"accessTokenExpiry\":1686748279312,\"refreshTokenExpiry\":1686758279312,\"accessTokenExpiryDate\":\"2023-06-14T14:11:19.312\",\"refreshTokenExpiryDate\":\"2023-06-14T16:57:59.312\",\"clock\":{\"instant\":{\"seconds\":1686738279,\"nanos\":312057100},\"zone\":{\"id\":\"Europe/London\"}}}";

    @Test
    public void testInitialiseFromTokenResponse(){
        String expectedAccessToken = "TestAccessToken";
        String expectedRefreshToken = "TestRefreshToken";
        TokenResponse testResponse = new TokenResponse();
        testResponse.expires_in = "10000";
        testResponse.refresh_expires_in = "20000";
        testResponse.access_token = expectedAccessToken;
        testResponse.refresh_token = expectedRefreshToken;
        testResponse.scope = "TestScope1,TestScope2";
        Instant instant = Instant.now();
        Clock fixedClock = Clock.fixed(instant,ZoneId.systemDefault());
        AuthenticationState.Builder builder = new AuthenticationState.Builder();
        AuthenticationState authenticationState = builder.fromTokenResponse(testResponse).withClock(fixedClock).build();
        Long accessTokenExpiry = fixedClock.millis() + TimeUnit.SECONDS.toMillis(10000L);
        LocalDateTime expectedAccessTokenExpiry = LocalDateTime.ofInstant(Instant.ofEpochMilli(accessTokenExpiry), ZoneId.systemDefault());
        Long refreshTokenExpiry = fixedClock.millis() + TimeUnit.SECONDS.toMillis(20000L);
        LocalDateTime expectedRefreshTokenExpiry = LocalDateTime.ofInstant(Instant.ofEpochMilli(refreshTokenExpiry), ZoneId.systemDefault());

        String[] expectedScopes = new String[2];
        expectedScopes[0] = "TestScope1";
        expectedScopes[1] = "TestScope2";
        String json = authenticationState.toJson();
        Assert.assertEquals(expectedAccessToken,authenticationState.getAccessToken());
        Assert.assertEquals(expectedRefreshToken,authenticationState.getRefreshToken());
        Assert.assertEquals(expectedAccessTokenExpiry,authenticationState.getAccessTokenExpiry());
        Assert.assertEquals(expectedRefreshTokenExpiry,authenticationState.getRefreshTokenExpiry());
        Assert.assertArrayEquals(expectedScopes,authenticationState.getScopes());
    }

    @Test
    public void testSetAccessTokenExpiry(){
        AuthenticationState state = new AuthenticationState();
        Clock fixedClock = Clock.fixed(Instant.now(),ZoneId.systemDefault());
        long accessTokenExpiry = fixedClock.millis() + TimeUnit.SECONDS.toMillis(10000L);
        LocalDateTime expectedAccessTokenExpiry = LocalDateTime.ofInstant(Instant.ofEpochMilli(accessTokenExpiry), ZoneId.systemDefault());
        state.setClock(fixedClock);
        state.setAccessTokenExpiry("10000");
        Assert.assertEquals(expectedAccessTokenExpiry,state.getAccessTokenExpiry());
    }
    @Test
    public void testSetRefreshTokenExpiry(){
        AuthenticationState state = new AuthenticationState();
        Clock fixedClock = Clock.fixed(Instant.now(),ZoneId.systemDefault());
        long refreshTokenExpiry = fixedClock.millis() + TimeUnit.SECONDS.toMillis(20000L);
        LocalDateTime expectedRefreshTokenExpiry = LocalDateTime.ofInstant(Instant.ofEpochMilli(refreshTokenExpiry), ZoneId.systemDefault());
        state.setClock(fixedClock);
        state.setRefreshTokenExpiry("20000");
        Assert.assertEquals(expectedRefreshTokenExpiry,state.getRefreshTokenExpiry());
    }

    @Test
    public void fromJsonTest(){
        AuthenticationState actualState = AuthenticationState.fromJson(authStateJson);
        AuthenticationState expectedState = createAuthenticationStateForTest();
        Assert.assertEquals(expectedState.getAccessToken(),actualState.getAccessToken());
        Assert.assertEquals(expectedState.getRefreshToken(),actualState.getRefreshToken());
        Assert.assertNotNull(actualState.getAccessTokenExpiry());
        Assert.assertNotNull(actualState.getRefreshTokenExpiry());
        Assert.assertArrayEquals(expectedState.getScopes(),actualState.getScopes());
    }

    private AuthenticationState createAuthenticationStateForTest(){
        String expectedAccessToken = "TestAccessToken";
        String expectedRefreshToken = "TestRefreshToken";
        TokenResponse testResponse = new TokenResponse();
        testResponse.expires_in = "10000";
        testResponse.refresh_expires_in = "20000";
        testResponse.access_token = expectedAccessToken;
        testResponse.refresh_token = expectedRefreshToken;
        testResponse.scope = "TestScope1,TestScope2";
        Instant instant = Instant.now();
        Clock fixedClock = Clock.fixed(instant,ZoneId.systemDefault());
        AuthenticationState.Builder builder = new AuthenticationState.Builder();
        AuthenticationState authenticationState = builder.fromTokenResponse(testResponse).withClock(fixedClock).build();
        return authenticationState;
    }

}
