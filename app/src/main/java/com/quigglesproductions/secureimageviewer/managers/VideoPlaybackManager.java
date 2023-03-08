package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import com.quigglesproductions.secureimageviewer.appauth.IAuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.ISecureDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.LocalFileDataSource;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VideoPlaybackManager {
    private static VideoPlaybackManager singleton;
    //private static MediaSession mediaSession;
    private Context rootContext;
    private ExoPlayer player;
    private long playbackPosition;
    private int currentWindowIndex;
    private boolean playWhenReady;
    private long seekBackIntervalMs = 10000;
    private long seekForwardIntervalMs = 10000;

    public static VideoPlaybackManager getInstance() {
        if(singleton == null)
            singleton = new VideoPlaybackManager();
        return singleton;
    }

    public ExoPlayer getExoPlayer(){
        if(player == null){
            player = new ExoPlayer.Builder(rootContext).build();
        }
        return player;
    }

    /*public MediaSession getMediaSession(){
        return mediaSession;
    }*/

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
        //mediaSession = new MediaSession.Builder(rootContext,player).build();

    }

    public void saveState() {
        playbackPosition = player.getCurrentPosition();
        playWhenReady = player.getPlayWhenReady();
    }

    public long getCurrentPosition() {
        return playbackPosition;
    }

    public boolean getPlayWhenReady() {
        return playWhenReady;
    }
    @OptIn(markerClass = UnstableApi.class)
    public Player getVideoFromNetwork(String fileUri, IAuthManager authManager, boolean playWhenReady) {
        authManager.performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                Map<String, String> headersMap = new HashMap<>();
                headersMap.put("Authorization", "Bearer " + accessToken);
                androidx.media3.datasource.DataSource.Factory factory = new DefaultHttpDataSource.Factory().setDefaultRequestProperties(headersMap);
                setExoPlayer(new ExoPlayer.Builder(rootContext).setMediaSourceFactory(new
                        DefaultMediaSourceFactory((androidx.media3.datasource.DataSource.Factory) factory)).setSeekBackIncrementMs(seekBackIntervalMs).setSeekForwardIncrementMs(seekForwardIntervalMs).build());
                androidx.media3.common.MediaItem mediaItem = MediaItem.fromUri(fileUri);
                player.setMediaItem(mediaItem);
                player.prepare();
                if(playWhenReady) {
                    player.play();
                }
            }
        });
        return player;
    }
    @OptIn(markerClass = UnstableApi.class)
    public void  getVideoFromDataSource(IFileDataSource dataSource, boolean playWhenReady, VideoPlayerCallback callback) {
        try {
            URL fileUrl = dataSource.getFileURL();
            File file = new File(String.valueOf(fileUrl));
            //if(dataSource instanceof LocalFileDataSource && !file.exists())
            //    throw new FileNotFoundException("File not found");
            androidx.media3.common.MediaItem mediaItem = MediaItem.fromUri(fileUrl.toString());
            if(ISecureDataSource.class.isAssignableFrom(dataSource.getClass())){
                ((ISecureDataSource)dataSource).getAuthorization().performActionWithFreshTokens(new AuthState.AuthStateAction() {
                    @Override
                    public void  execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
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
                        callback.VideoPlayerRecieved(player,null);
                    }
                });
            }
            else{
                setExoPlayer(new ExoPlayer.Builder(rootContext).build());
                player.setMediaItem(mediaItem);
                player.prepare();
                if(playWhenReady) {
                    player.play();
                }
                callback.VideoPlayerRecieved(player,null);
            }
        }
        catch(MalformedURLException | RequestServiceNotConfiguredException exception){
            callback.VideoPlayerRecieved(null,exception);
        }
    }

    public interface VideoPlayerCallback{
        void VideoPlayerRecieved(ExoPlayer player, Exception exception);
    }
}
