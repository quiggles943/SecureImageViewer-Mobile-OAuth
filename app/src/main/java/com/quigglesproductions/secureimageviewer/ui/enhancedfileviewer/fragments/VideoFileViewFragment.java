package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;

public class VideoFileViewFragment extends BaseFileViewFragment {
    PlayerView videoView;
    private static final boolean PLAY_ON_LOAD = false;
    private static final String PLAYER_IS_READY_KEY = "exoplayer.play_when_ready";
    private static final String PLAYER_CURRENT_POS_KEY = "exoplayer.pos";
    private ExoPlayer mPlayer;
    private boolean isPlaying;
    public VideoFileViewFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_video_view, container, false);
    }

    @Override
    @OptIn(markerClass = UnstableApi.class)
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        videoView = view.findViewById(R.id.videoView);
        /*videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getNavigator().isFullyVisible())
                    getNavigator().hide();
                else
                    getNavigator().show();
            }
        });*/
        IDisplayFile file = getFile();
        loadVideo(view,file,savedInstanceState);
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    @OptIn(markerClass = UnstableApi.class)
    public void onResume() {
        super.onResume();
        if(getViewerNavigator().isFullyVisible()){
            videoView.showController();
        }
        else
            videoView.hideController();
        videoView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if(getViewerNavigator() == null)
                    return;
                switch(visibility){
                    case 0:
                        getViewerNavigator().show();
                        break;
                    case 8:
                        getViewerNavigator().hide();
                        break;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getActivity().isChangingConfigurations()){

        }
        else{
            if(videoView.getPlayer() != null)
                videoView.getPlayer().pause();
        }

    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadVideo(View itemView, IDisplayFile item, Bundle savedInstanceState){
        try {
            if (itemView != null && mPlayer == null) {

                VideoPlaybackManager.getInstance().getVideoFromDataSource(item.getDataSource(), PLAY_ON_LOAD, new VideoPlaybackManager.VideoPlayerCallback() {
                    @Override
                    public void VideoPlayerRecieved(ExoPlayer player, Exception exception) {
                        mPlayer = player;
                        videoView.setPlayer(mPlayer);
                        mPlayer.addListener(new Player.Listener(){
                            @Override
                            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                                if (playWhenReady && playbackState == Player.STATE_READY) {
                                    // media actually playing
                                    isPlaying = true;
                                } else if (playWhenReady) {
                                    // might be idle (plays after prepare()),
                                    // buffering (plays when data available)
                                    // or ended (plays when seek away from end)
                                    isPlaying = false;
                                } else {
                                    // player paused in any state
                                    isPlaying = false;
                                }
                            }
                        });
                        resumePlayback(savedInstanceState);
                        if(getViewerNavigator() == null)
                            return;

                    }
                });

            }
            else if(mPlayer != null){
                videoView.setPlayer(mPlayer);
                if(isPlaying)
                    mPlayer.play();
                else
                    mPlayer.pause();
            }

        } catch (Exception exc) {
            Log.e("Error", exc.getMessage());
        }
    }

    private void updateSeekBarProgress(){
        return;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPlayer != null) {
            outState.putLong(PLAYER_CURRENT_POS_KEY, Math.max(0, mPlayer.getCurrentPosition()));
            if(mPlayer.getPlaybackState() == Player.STATE_READY && mPlayer.getPlayWhenReady())
                outState.putBoolean(PLAYER_IS_READY_KEY, true);
            else
                outState.putBoolean(PLAYER_IS_READY_KEY,false);
        }
        else{
            outState.putLong(PLAYER_CURRENT_POS_KEY, 0);
            outState.putBoolean(PLAYER_IS_READY_KEY, false);
        }
    }

    private boolean resumePlayback(@Nullable Bundle inState){
        if(inState == null)
            return false;
        mPlayer.setPlayWhenReady(inState.getBoolean(PLAYER_IS_READY_KEY));
        mPlayer.seekTo(inState.getLong(PLAYER_CURRENT_POS_KEY));
        return true;
    }
}
