package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager.VideoPlayerCallback
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewerViewModelKt
import javax.inject.Inject

class VideoFileViewFragmentKt(playbackManager: VideoPlaybackManager,
                              val viewModel: EnhancedFileViewerViewModelKt
    ) : BaseFileViewFragmentKt() {
    var videoView: PlayerView? = null
    private var mPlayer: ExoPlayer? = null
    private var isPlaying = false
    var playbackManager: VideoPlaybackManager = playbackManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_video_view, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        videoView = view.findViewById(R.id.videoView)
        val file = file
        loadVideo(view, file, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        if (viewerNavigator!!.isFullyVisible) {
            videoView!!.showController()
        } else videoView!!.hideController()
        videoView!!.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            if (viewerNavigator == null) return@ControllerVisibilityListener
            when (visibility) {
                0 -> viewerNavigator!!.show()
                8 -> viewerNavigator!!.hide()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (requireActivity().isChangingConfigurations) {
        } else {
            if (videoView!!.player != null) videoView!!.player!!.pause()
        }
    }


    private fun loadVideo(itemView: View?, item: IDisplayFile?, savedInstanceState: Bundle?) {
        try {
            if (itemView != null && mPlayer == null) {
                playbackManager.getVideoFromDataSource(
                    item!!.dataSource,
                    PLAY_ON_LOAD,
                    VideoPlayerCallback { player, exception ->
                        mPlayer = player
                        videoView!!.player = mPlayer
                        mPlayer!!.addListener(object : Player.Listener {
                            @OptIn(UnstableApi::class)
                            override fun onPlayerStateChanged(
                                playWhenReady: Boolean,
                                playbackState: Int
                            ) {
                                isPlaying =
                                    if (playWhenReady && playbackState == Player.STATE_READY) {
                                        // media actually playing
                                        true
                                    } else if (playWhenReady) {
                                        // might be idle (plays after prepare()),
                                        // buffering (plays when data available)
                                        // or ended (plays when seek away from end)
                                        false
                                    } else {
                                        // player paused in any state
                                        false
                                    }
                            }
                        })
                        resumePlayback(savedInstanceState)
                        if (viewerNavigator == null) return@VideoPlayerCallback
                    })
            } else if (mPlayer != null) {
                videoView!!.player = mPlayer
                if (isPlaying) mPlayer!!.play() else mPlayer!!.pause()
            }
        } catch (exc: Exception) {
            Log.e("Error", exc.message!!)
        }
    }

    private fun updateSeekBarProgress() {
        return
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mPlayer != null) {
            viewModel.videoPosition.value = Math.max(0, mPlayer!!.currentPosition)
            outState.putLong(PLAYER_CURRENT_POS_KEY, Math.max(0, mPlayer!!.currentPosition))
            if (mPlayer!!.playbackState == Player.STATE_READY && mPlayer!!.playWhenReady) {
                outState.putBoolean(
                    PLAYER_IS_READY_KEY, true
                )
                viewModel.videoPlayback.value = true
            }else {
                outState.putBoolean(PLAYER_IS_READY_KEY, false)
                viewModel.videoPlayback.value = false
            }
        } else {
            outState.putLong(PLAYER_CURRENT_POS_KEY, 0)
            viewModel.videoPosition.value = 0
            outState.putBoolean(PLAYER_IS_READY_KEY, false)
            viewModel.videoPlayback.value = false
        }
    }

    private fun resumePlayback(inState: Bundle?): Boolean {
        if (inState == null) return false
        mPlayer!!.playWhenReady = inState.getBoolean(PLAYER_IS_READY_KEY)
        mPlayer!!.seekTo(inState.getLong(PLAYER_CURRENT_POS_KEY))

        mPlayer!!.playWhenReady = viewModel.videoPlayback.value!!
        mPlayer!!.seekTo(viewModel.videoPosition.value!!)
        return true
    }

    companion object {
        private const val PLAY_ON_LOAD = false
        private const val PLAYER_IS_READY_KEY = "exoplayer.play_when_ready"
        private const val PLAYER_CURRENT_POS_KEY = "exoplayer.pos"
    }
}
