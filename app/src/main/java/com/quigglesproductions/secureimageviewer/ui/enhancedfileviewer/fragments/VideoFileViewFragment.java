package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;

public class VideoFileViewFragment extends BaseFileViewFragment {
    PlayerView videoView;
    private static final boolean PLAY_ON_LOAD = false;
    private FileViewerNavigator navigatorControls;
    public VideoFileViewFragment(FileViewerNavigator viewerNavigator){
        navigatorControls = viewerNavigator;
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
        EnhancedFile file = getFile();
        loadVideo(view,file);
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    @OptIn(markerClass = UnstableApi.class)
    public void onResume() {
        super.onResume();
        if(navigatorControls.isFullyVisible()){
            videoView.showController();
        }
        else
            videoView.hideController();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(videoView.getPlayer() != null)
            videoView.getPlayer().pause();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadVideo(View itemView, EnhancedFile item){
        try {
            if (itemView != null) {

                VideoPlaybackManager.getInstance().getVideoFromDataSource(item.getDataSource(), PLAY_ON_LOAD, new VideoPlaybackManager.VideoPlayerCallback() {
                    @Override
                    public void VideoPlayerRecieved(ExoPlayer player, Exception exception) {
                        videoView.setPlayer(player);
                        if(navigatorControls.isFullyVisible())
                            videoView.showController();
                        else
                            videoView.hideController();
                    }
                });
                videoView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
                    @Override
                    public void onVisibilityChanged(int visibility) {
                        switch(visibility){
                            case 0:
                                navigatorControls.show();
                                break;
                            case 8:
                                navigatorControls.hide();
                                break;
                        }
                    }
                });
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
}
