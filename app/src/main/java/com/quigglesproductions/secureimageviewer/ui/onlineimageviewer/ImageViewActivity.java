package com.quigglesproductions.secureimageviewer.ui.onlineimageviewer;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.utils.ImageUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ImageViewActivity extends SecureActivity implements ViewPager.OnPageChangeListener {
    ViewPager mPager;
    Gson gson;
    TextView fileName;
    LinearLayout topLayout;
    ViewPagerAdapter mViewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        setContentView(R.layout.activity_image_pager);
        //FolderModel folder = gson.fromJson(getIntent().getStringExtra("folder"), FolderModel.class);
        Type listType = new TypeToken<ArrayList<FileModel>>(){}.getType();
        ArrayList<FileModel> files = gson.fromJson(getIntent().getStringExtra("fileList"),listType);
        int selectedPosition = (int) getIntent().getIntExtra("position",0);
        mPager = (ViewPager) findViewById(R.id.view_pager);
        fileName = findViewById(R.id.file_name);
        topLayout = findViewById(R.id.topLinearLayout);
        mViewPagerAdapter = new ViewPagerAdapter(ImageViewActivity.this, files);

        mViewPagerAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(topLayout.getVisibility() == View.VISIBLE){
                    topLayout.setVisibility(View.INVISIBLE);
                }
                else
                    topLayout.setVisibility(View.VISIBLE);
            }
        });

        // Adding the Adapter to the ViewPager
        mPager.setAdapter(mViewPagerAdapter);
        mPager.addOnPageChangeListener(this);
        mPager.setCurrentItem(selectedPosition);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            ViewGroup.LayoutParams params = topLayout.getLayoutParams();
            params.height = ImageUtils.dpToPx(60,this);
            topLayout.setLayoutParams(params);
        } else {
            // In portrait
            ViewGroup.LayoutParams params = topLayout.getLayoutParams();
            params.height = ImageUtils.dpToPx(80,this);
            topLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        fileName.setText(mViewPagerAdapter.getPageTitle(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}