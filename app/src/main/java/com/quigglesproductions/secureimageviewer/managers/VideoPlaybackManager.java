package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.FileDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;

import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;

public class VideoPlaybackManager {
    private final Context rootContext;
    private ExoPlayer player;
    private boolean playWhenReady;
    private final long seekBackIntervalMs = 10000;
    private final long seekForwardIntervalMs = 10000;
    private final OkHttpClient client;

    public VideoPlaybackManager(Context context, OkHttpClient client){
        rootContext = context.getApplicationContext();
        this.client = client;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void releasePlayer(){
        if (player != null) {
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
                    callback.VideoPlayerReceived(player, null);
                }
                case ONLINE -> {
                    setupExoPlayerStreaming(mediaItem);
                    callback.VideoPlayerReceived(player, null);
                }
            }

        }
        catch(MalformedURLException exception){
            callback.VideoPlayerReceived(null,exception);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupExoPlayerStreaming(MediaItem mediaItem){

        DataSource.Factory factory = new OkHttpDataSource.Factory(client);
        setExoPlayer(new ExoPlayer.Builder(rootContext).setMediaSourceFactory(new
                DefaultMediaSourceFactory(factory)).setSeekBackIncrementMs(seekBackIntervalMs).setSeekForwardIncrementMs(seekForwardIntervalMs).build());
        player.setMediaItem(mediaItem);
        player.prepare();
        if(playWhenReady) {
            player.play();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupExoPlayerLocal(MediaItem mediaItem){
        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new FileDataSource.Factory());
        ProgressiveMediaSource mediaSource = factory.createMediaSource(mediaItem);
        setExoPlayer(new ExoPlayer.Builder(rootContext).setSeekBackIncrementMs(seekBackIntervalMs).setSeekForwardIncrementMs(seekForwardIntervalMs).build());
        player.setMediaSource(mediaSource);
        player.prepare();
        if(playWhenReady) {
            player.play();
        }
    }

    public interface VideoPlayerCallback{
        void VideoPlayerReceived(ExoPlayer player, Exception exception);
    }
}
