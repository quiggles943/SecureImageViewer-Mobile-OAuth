package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.FileDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VideoPlaybackManager {
    private static VideoPlaybackManager singleton;
    private Context rootContext;
    private ExoPlayer player;
    private long playbackPosition;
    private int currentWindowIndex;
    private boolean playWhenReady;
    private long seekBackIntervalMs = 10000;
    private long seekForwardIntervalMs = 10000;
    private AuroraAuthenticationManager authenticationManager;

    public VideoPlaybackManager(Context context, AuroraAuthenticationManager authenticationManager){
        rootContext = context.getApplicationContext();
        this.authenticationManager = authenticationManager;
    }

    public void setContext(Context context){
        rootContext = context.getApplicationContext();
    }

    @OptIn(markerClass = UnstableApi.class)
    public void releasePlayer(){
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindowIndex = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    public void setExoPlayer(ExoPlayer exoPlayer) {

        player = exoPlayer;

    }

    @OptIn(markerClass = UnstableApi.class)
    public void  getVideoFromDataSource(IFileDataSource dataSource, boolean playWhenReady, VideoPlayerCallback callback) {
        try {
            URL fileUrl = dataSource.getFileURL();
            androidx.media3.common.MediaItem mediaItem = MediaItem.fromUri(fileUrl.toString());
            switch (dataSource.getFileSourceType()) {
                case LOCAL -> {
                    setupExoPlayerLocal(mediaItem);
                    callback.VideoPlayerRecieved(player, null);
                }
                case ONLINE ->
                        authenticationManager.performActionWithFreshTokens(rootContext, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                                setupExoPlayerStreaming(mediaItem, accessToken);
                                callback.VideoPlayerRecieved(player, null);
                            }
                        });
            }

        }
        catch(MalformedURLException exception){
            callback.VideoPlayerRecieved(null,exception);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer setupExoPlayerStreaming(MediaItem mediaItem, String accessToken){
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Bearer " + accessToken);
        DataSource.Factory factory = new DefaultHttpDataSource.Factory().setDefaultRequestProperties(headersMap);
        setExoPlayer(new ExoPlayer.Builder(rootContext).setMediaSourceFactory(new
                DefaultMediaSourceFactory((DataSource.Factory) factory)).setSeekBackIncrementMs(seekBackIntervalMs).setSeekForwardIncrementMs(seekForwardIntervalMs).build());
        player.setMediaItem(mediaItem);
        player.prepare();
        if(playWhenReady) {
            player.play();
        }
        return player;
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer setupExoPlayerLocal(MediaItem mediaItem){
        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new FileDataSource.Factory());
        ProgressiveMediaSource mediaSource = factory.createMediaSource(mediaItem);
        setExoPlayer(new ExoPlayer.Builder(rootContext).setSeekBackIncrementMs(seekBackIntervalMs).setSeekForwardIncrementMs(seekForwardIntervalMs).build());
        player.setMediaSource(mediaSource);
        player.prepare();
        if(playWhenReady) {
            player.play();
        }
        return player;
    }

    public interface VideoPlayerCallback{
        void VideoPlayerRecieved(ExoPlayer player, Exception exception);
    }
}
