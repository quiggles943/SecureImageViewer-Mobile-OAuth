package com.quigglesproductions.secureimageviewer.authentication;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenManagerTest {

    @Mock
    TokenManager mockedTokenManager;
    @Spy
    TokenManager tokenManagerSpy;
    @Mock
    Context context;
    @Mock
    SharedPreferences sharedPreferences;

    String defaultJson = "{\"accessToken\":\"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoiUGF1bCIsImNsaWVudF9pZCI6ImJmMjg5YTNmLWFlZDYtNDI1Mi05NmIzLTIwN2UyOTljNzM1NyIsInNjcCI6InJlYWQgIHdyaXRlIiwianRpIjoiNjdhNzY2ZTQtMzRkMC00MDI5LWE0OTMtYmNjOGZiOTIxY2U2IiwiYXVkIjoicXVpZ2xleXNlcnZlci5kZG5zLm5ldCIsInVzZXJJZCI6IjE4ODQ5MjE4LTNhMjEtNDQ4ZC1iYjZiLTYxZWRmOThhY2NmNSIsImV4cCI6MTY4NjY5NjMzOCwiaXNzIjoiaHR0cHM6Ly93d3cucXVpZ2xleWlkLmRkbnMubmV0In0.mTin_gp-bGzZ-R-lbz52E-MLIDGAoWzB653-a82MzukaycztGNCyraNLfkLYnzEps7zf0KtPks2WZ_p2xpATiQclDuk0oMIfbNOgELhg2mCPZUb9qnLi-fduCP1cAWMLwpEfC-3SxqD2QuBScabq7MaDGe8Hl3yyRwa_ubw-qt4zVhfxkxQYoEn_bRmUKEPELoknDhxKB16OehJpAfMbunNqzpSp814IH0KeL2zxgoYVnAFURO25HGYBqz46LjdK9UcSSVZbt2mRM79YLqrc62fyMm_WQIfohcuZqKiqIlLkfVIuEbER6bECZs8pmPsN-w-mBCZ01nAocME8744cDw\",\"accessTokenExpiry\":1686696338054,\"accessTokenExpiryDate\":\"2023-06-13T23:45:38.054\",\"refreshToken\":\"Of7Fpl2VWnJFbskDjHAy/Y0EoEKulgE0+OoRsNfZPQ0\\u003d\",\"refreshTokenExpiry\":1687299338073,\"refreshTokenExpiryDate\":\"2023-06-20T23:15:38.073\",\"scopes\":[\"read  write\"]}";

    AuthenticationState validAuthenticationState;
    @Before
    public void setup(){
        Mockito.when(context.getSharedPreferences(anyString(),anyInt())).thenReturn(sharedPreferences);
        tokenManagerSpy.setContext(context);
        validAuthenticationState = ViewerGson.getGson().fromJson(defaultJson,AuthenticationState.class);

    }
    @Test
    public void getTokenPrefTest(){
        SharedPreferences actualPreferences = tokenManagerSpy.getTokenPref();
        Assert.assertEquals(sharedPreferences,actualPreferences);
    }

    @Test
    public void getAuthenticationState(){
        Mockito.when(tokenManagerSpy.getTokenPref().getString("com.secureimageviewer.preference.authentication.token.authstate",null))
                .thenReturn(defaultJson);
        AuthenticationState actualState = tokenManagerSpy.getAuthenticationState();
        Assert.assertEquals(validAuthenticationState.getAccessToken(),actualState.getAccessToken());
        Assert.assertEquals(validAuthenticationState.getAccessTokenExpiry(),actualState.getAccessTokenExpiry());
        Assert.assertEquals(validAuthenticationState.getRefreshToken(),actualState.getRefreshToken());
        Assert.assertEquals(validAuthenticationState.getRefreshTokenExpiry(),actualState.getRefreshTokenExpiry());
    }

    @Test
    public void getAuthenticationStateWhenNotSetTest(){
        AuthenticationState authenticationState = tokenManagerSpy.getAuthenticationState();
        Assert.assertNull(authenticationState);
    }

    @Test
    public void setAuthenticationStateTest(){
        SharedPreferences.Editor mockEditor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPreferences.edit()).thenReturn(mockEditor);
        tokenManagerSpy.setAuthenticaionState(validAuthenticationState);
        verify(mockEditor,times(1)).putString("com.secureimageviewer.preference.authentication.token.authstate","{\"accessToken\":\"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoiUGF1bCIsImNsaWVudF9pZCI6ImJmMjg5YTNmLWFlZDYtNDI1Mi05NmIzLTIwN2UyOTljNzM1NyIsInNjcCI6InJlYWQgIHdyaXRlIiwianRpIjoiNjdhNzY2ZTQtMzRkMC00MDI5LWE0OTMtYmNjOGZiOTIxY2U2IiwiYXVkIjoicXVpZ2xleXNlcnZlci5kZG5zLm5ldCIsInVzZXJJZCI6IjE4ODQ5MjE4LTNhMjEtNDQ4ZC1iYjZiLTYxZWRmOThhY2NmNSIsImV4cCI6MTY4NjY5NjMzOCwiaXNzIjoiaHR0cHM6Ly93d3cucXVpZ2xleWlkLmRkbnMubmV0In0.mTin_gp-bGzZ-R-lbz52E-MLIDGAoWzB653-a82MzukaycztGNCyraNLfkLYnzEps7zf0KtPks2WZ_p2xpATiQclDuk0oMIfbNOgELhg2mCPZUb9qnLi-fduCP1cAWMLwpEfC-3SxqD2QuBScabq7MaDGe8Hl3yyRwa_ubw-qt4zVhfxkxQYoEn_bRmUKEPELoknDhxKB16OehJpAfMbunNqzpSp814IH0KeL2zxgoYVnAFURO25HGYBqz46LjdK9UcSSVZbt2mRM79YLqrc62fyMm_WQIfohcuZqKiqIlLkfVIuEbER6bECZs8pmPsN-w-mBCZ01nAocME8744cDw\",\"refreshToken\":\"Of7Fpl2VWnJFbskDjHAy/Y0EoEKulgE0+OoRsNfZPQ0\\u003d\",\"scopes\":[\"read  write\"],\"accessTokenExpiry\":1686696338054,\"refreshTokenExpiry\":1687299338073,\"accessTokenExpiryDate\":\"2023-06-13T23:45:38.054\",\"refreshTokenExpiryDate\":\"2023-06-20T23:15:38.073\"}");

    }

    @Test
    public void getAccessTokenWithAuthenticationStateTest(){
        Mockito.when(tokenManagerSpy.getTokenPref().getString("com.secureimageviewer.preference.authentication.token.authstate",null))
                .thenReturn(defaultJson);
        String expectedAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoiUGF1bCIsImNsaWVudF9pZCI6ImJmMjg5YTNmLWFlZDYtNDI1Mi05NmIzLTIwN2UyOTljNzM1NyIsInNjcCI6InJlYWQgIHdyaXRlIiwianRpIjoiNjdhNzY2ZTQtMzRkMC00MDI5LWE0OTMtYmNjOGZiOTIxY2U2IiwiYXVkIjoicXVpZ2xleXNlcnZlci5kZG5zLm5ldCIsInVzZXJJZCI6IjE4ODQ5MjE4LTNhMjEtNDQ4ZC1iYjZiLTYxZWRmOThhY2NmNSIsImV4cCI6MTY4NjY5NjMzOCwiaXNzIjoiaHR0cHM6Ly93d3cucXVpZ2xleWlkLmRkbnMubmV0In0.mTin_gp-bGzZ-R-lbz52E-MLIDGAoWzB653-a82MzukaycztGNCyraNLfkLYnzEps7zf0KtPks2WZ_p2xpATiQclDuk0oMIfbNOgELhg2mCPZUb9qnLi-fduCP1cAWMLwpEfC-3SxqD2QuBScabq7MaDGe8Hl3yyRwa_ubw-qt4zVhfxkxQYoEn_bRmUKEPELoknDhxKB16OehJpAfMbunNqzpSp814IH0KeL2zxgoYVnAFURO25HGYBqz46LjdK9UcSSVZbt2mRM79YLqrc62fyMm_WQIfohcuZqKiqIlLkfVIuEbER6bECZs8pmPsN-w-mBCZ01nAocME8744cDw";
        String actualAccessToken = tokenManagerSpy.getAccessToken();
        Assert.assertEquals(expectedAccessToken,actualAccessToken);
    }

    @Test
    public void getAccessTokenWithNoAuthenticationStateTest(){
        Mockito.when(tokenManagerSpy.getTokenPref().getString("com.secureimageviewer.preference.authentication.token.authstate",null))
                .thenReturn(null);
        String actualAccessToken = tokenManagerSpy.getAccessToken();
        Assert.assertNull(actualAccessToken);
    }

    @Test
    public void getRefreshTokenWithAuthenticationStateTest(){
        Mockito.when(tokenManagerSpy.getTokenPref().getString("com.secureimageviewer.preference.authentication.token.authstate",null))
                .thenReturn(defaultJson);
        String expectedRefreshToken = "Of7Fpl2VWnJFbskDjHAy/Y0EoEKulgE0+OoRsNfZPQ0=";
        String actualRefreshToken = tokenManagerSpy.getRefreshToken();
        Assert.assertEquals(expectedRefreshToken,actualRefreshToken);
    }

    @Test
    public void getRefreshTokenWithNoAuthenticationStateTest(){
        Mockito.when(tokenManagerSpy.getTokenPref().getString("com.secureimageviewer.preference.authentication.token.authstate",null))
                .thenReturn(null);
        String actualRefreshToken = tokenManagerSpy.getRefreshToken();
        Assert.assertNull(actualRefreshToken);
    }

}
