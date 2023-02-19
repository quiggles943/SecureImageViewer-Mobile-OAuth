package com.quigglesproductions.secureimageviewer.ui.newimageviewer;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.listeners.AdapterInstantiatedListener;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;

public class FileViewActivity extends SecureActivity implements ViewPager.OnPageChangeListener {
    private static final String KEY_FILE_POSITION = "viewerPager.position";
    private static final String KEY_PLAYER_POSITION = "player.position";
    private static final String KEY_PLAYER_PLAY_WHEN_READY = "player.playWhenReady";
    ViewPager mPager;
    Gson gson;
    TextView fileName;
    LinearLayout topLayout;
    //LinearLayout bottomLayout;
    FileViewerNavigator fileViewerNavigatorImage;
    FileViewerNavigator fileViewerNavigatorVideo;
    LinearLayout imagePagerControls;
    ViewPagerAdapter mViewPagerAdapter;
    Context context;
    //ExoPlayer exoPlayer;
    Bundle currentState;
    int currentIndex;
    //TextView imageCountText,imageTotalText;
    View.OnClickListener prevFileClick,nextFileClick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        context = this;
        currentState = savedInstanceState;
        setContentView(R.layout.activity_image_pager);
        EnhancedFolder selectedFolder = FolderManager.getInstance().getCurrentFolder();
        int selectedPosition = (int) getIntent().getIntExtra("position",0);
        mPager = (ViewPager) findViewById(R.id.view_pager);

        setupControls();
        mViewPagerAdapter = new ViewPagerAdapter(FileViewActivity.this);
        if(EnhancedDatabaseFolder.class.isAssignableFrom(selectedFolder.getClass())) {
            mViewPagerAdapter.addFiles(((EnhancedDatabaseFolder)selectedFolder).getBaseItems());
        }
        else if (EnhancedOnlineFolder.class.isAssignableFrom(selectedFolder.getClass())) {
            mViewPagerAdapter.addFiles(((EnhancedOnlineFolder)selectedFolder).getBaseItems());
        }
        fileViewerNavigatorImage.setTotal(mViewPagerAdapter.getCount());
        //imageTotalText.setText(mViewPagerAdapter.getCount()+"");
        mViewPagerAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(topLayout.getVisibility() == View.VISIBLE){
                    topLayout.setVisibility(View.INVISIBLE);
                    if(fileViewerNavigatorImage != null)
                        fileViewerNavigatorImage.setVisibility(View.INVISIBLE);
                    if(fileViewerNavigatorVideo != null)
                        fileViewerNavigatorVideo.setVisibility(View.INVISIBLE);
                }
                else {
                    topLayout.setVisibility(View.VISIBLE);
                    if(fileViewerNavigatorImage != null)
                        fileViewerNavigatorImage.setVisibility(View.VISIBLE);
                    if(fileViewerNavigatorVideo != null)
                        fileViewerNavigatorVideo.setVisibility(View.VISIBLE);
                }
            }
        });
        mViewPagerAdapter.setAdapterInstantiatedListener(new AdapterInstantiatedListener() {
            @Override
            public void onAdapterInstantiated() {
                onPageSelected(selectedPosition);
            }
        });
        mPager.setAdapter(mViewPagerAdapter);
        mPager.addOnPageChangeListener(this);
        if(savedInstanceState != null){
            currentIndex = savedInstanceState.getInt(KEY_FILE_POSITION);
            mPager.setCurrentItem(savedInstanceState.getInt("currentPos"));
            VideoPlaybackManager.getInstance().getExoPlayer().seekTo(savedInstanceState.getLong(KEY_PLAYER_POSITION));
            VideoPlaybackManager.getInstance().getExoPlayer().setPlayWhenReady(savedInstanceState.getBoolean(KEY_PLAYER_PLAY_WHEN_READY));
        }
        mPager.setCurrentItem(selectedPosition);
        hideSystemBars();
    }

    private void hideSystemBars(){
        View decorView = getWindow().getDecorView();
        WindowInsetsController windowInsetsController = decorView.getWindowInsetsController();
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsets.Type.systemBars());
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
    }

    private void setupControls(){
        prevFileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(mPager.getCurrentItem()-1);
            }
        };
        nextFileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(mPager.getCurrentItem()+1);
            }
        };
        fileName = findViewById(R.id.file_name);
        topLayout = findViewById(R.id.topLinearLayout);
        //bottomLayout = findViewById(R.id.imageviewer_pager_layout);
        fileViewerNavigatorImage = findViewById(R.id.fileviewer_navigator);
        fileViewerNavigatorVideo = findViewById(R.id.fileviewer_navigator_video);
        imagePagerControls = findViewById(R.id.image_pager_controls);
        setupImageNavigatorControls();
        setupVideoNavigatorControls();


        ImageButton backButton = topLayout.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //imageCountText = findViewById(R.id.imagecount);
        //imageTotalText = findViewById(R.id.imagetotal);

    }

    private void setupImageNavigatorControls(){
        if(fileViewerNavigatorImage != null) {
            ImageButton prevButtonImage = fileViewerNavigatorImage.findViewById(R.id.imagepager_prev);
            prevButtonImage.setOnClickListener(prevFileClick);
            ImageButton nextButtonImage = fileViewerNavigatorImage.findViewById(R.id.imagepager_next);
            nextButtonImage.setOnClickListener(nextFileClick);
        }
    }
    private void setupVideoNavigatorControls(){
        if(fileViewerNavigatorVideo != null) {
            ImageButton prevButtonImage = fileViewerNavigatorVideo.findViewById(R.id.imagepager_prev);
            prevButtonImage.setOnClickListener(prevFileClick);
            ImageButton nextButtonImage = fileViewerNavigatorVideo.findViewById(R.id.imagepager_next);
            nextButtonImage.setOnClickListener(nextFileClick);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private void setNavigatorPosition(int position){
        if(fileViewerNavigatorImage != null)
            fileViewerNavigatorImage.setPosition(position+1);
        if(fileViewerNavigatorVideo != null)
            fileViewerNavigatorVideo.setPosition(position+1);
    }

    @Override
    public void onPageSelected(int position) {
        fileName.setText(mViewPagerAdapter.getPageTitle(position));
        if (VideoPlaybackManager.getInstance().getExoPlayer() != null)
            VideoPlaybackManager.getInstance().getExoPlayer().stop();
        EnhancedFile item = mViewPagerAdapter.getItem(position);
        setNavigatorPosition(position+1);
        //fileViewerNavigatorImage.setPosition(position+1);
        //fileViewerNavigatorVideo.setPosition(position+1);
        //imageCountText.setText((position+1)+"");
        if (item.metadata.fileType.equalsIgnoreCase("VIDEO")) {
            topLayout.setVisibility(View.INVISIBLE);
            fileViewerNavigatorImage.setVisibility(View.INVISIBLE);
            try {
                View view = mPager.findViewWithTag(position);
                if (view != null) {
                    PlayerView videoView = view.findViewById(R.id.videoView);
                    fileViewerNavigatorVideo = videoView.findViewById(R.id.fileviewer_navigator_video);
                    setupVideoNavigatorControls();
                    setNavigatorPosition(position+1);
                    fileViewerNavigatorVideo.setTotal(mViewPagerAdapter.getCount());
                    ImageButton backButton = videoView.findViewById(R.id.backButton);
                    backButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
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
                    currentIndex = position;
                }
            } catch (Exception exc) {
                Log.e("Error", exc.getMessage());
            }
        }
        else{
            if(fileViewerNavigatorImage == null) {
                fileViewerNavigatorImage = findViewById(R.id.fileviewer_navigator);
                setupImageNavigatorControls();
                setNavigatorPosition(position+1);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoPlaybackManager.getInstance().saveState();
        VideoPlaybackManager.getInstance().releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList("myAdapter", mViewPagerAdapter.getItems());
        state.putInt("currentPos",mPager.getCurrentItem());
        long currentPos = VideoPlaybackManager.getInstance().getCurrentPosition();
        state.putLong(KEY_PLAYER_POSITION, currentPos);
        state.putBoolean(KEY_PLAYER_PLAY_WHEN_READY, VideoPlaybackManager.getInstance().getPlayWhenReady());
        state.putInt(KEY_FILE_POSITION,currentIndex);
    }
}