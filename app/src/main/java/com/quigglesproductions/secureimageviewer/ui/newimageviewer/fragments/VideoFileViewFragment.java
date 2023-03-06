package com.quigglesproductions.secureimageviewer.ui.newimageviewer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;

public class VideoFileViewFragment extends BaseFileViewFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_video_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        EnhancedFile file = ViewerGson.getGson().fromJson(args.getString(ARG_FILE),EnhancedFile.class);
        loadVideo(view,file);
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadVideo(View itemView, EnhancedFile item){
        try {
            if (itemView != null) {
                PlayerView videoView = itemView.findViewById(R.id.videoView);
                //fileViewerNavigatorVideo = videoView.findViewById(R.id.fileviewer_navigator_video);
                ImageButton backButton = videoView.findViewById(R.id.backButton);
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
                TextView titleView = videoView.findViewById(R.id.exo_title);
                titleView.setText(item.getName());
                VideoPlaybackManager.getInstance().getVideoFromDataSource(item.getDataSource(), true, new VideoPlaybackManager.VideoPlayerCallback() {
                    @Override
                    public void VideoPlayerRecieved(ExoPlayer player, Exception exception) {
                        videoView.setPlayer(player);
                    }
                });
                //videoView.setPlayer(VideoPlaybackManager.getInstance().getVideoFromNetwork(fullUri,AuthManager.getInstance(),true));
            }
        } catch (Exception exc) {
            Log.e("Error", exc.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
