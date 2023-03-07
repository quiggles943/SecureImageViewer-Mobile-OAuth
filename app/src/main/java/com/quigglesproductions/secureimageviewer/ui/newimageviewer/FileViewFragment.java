package com.quigglesproductions.secureimageviewer.ui.newimageviewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;

import java.lang.reflect.Field;

public class FileViewFragment extends Fragment {
    private int startPos;
    ViewPager2 viewPager;
    LinearLayout topLayout;
    ImageButton backButton;
    TextView fileName;
    FileViewerNavigator fileNavigator;
    EnhancedFile selectedFile;
    int currentPagerSlopMultiplier;
    public FileViewFragment(int startPosition){
        startPos = startPosition;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_pager,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.fragment_view_pager);
        NewFileCollectionAdapter collectionAdapter = new NewFileCollectionAdapter(this);
        EnhancedFolder selectedFolder = FolderManager.getInstance().getCurrentFolder();
        collectionAdapter.addFiles(selectedFolder.getBaseItems());
        viewPager.setAdapter(collectionAdapter);
        super.onViewCreated(view, savedInstanceState);
        setupNavigationControls(view);
        fileNavigator.setTotal(collectionAdapter.getItemCount());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectedFile = collectionAdapter.getItem(position);
                fileName.setText(selectedFile.getName());
                fileNavigator.setPosition(position+1);
                topLayout.invalidate();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        });
        viewPager.setCurrentItem(startPos,false);
        selectedFile = collectionAdapter.getItem(startPos);
        fileName.setText(selectedFile.getName());
        viewPager.setNestedScrollingEnabled(true);
        collectionAdapter.setFileZoomLevelCallback(new NewFileCollectionAdapter.ZoomLevelChangeCallback() {
            @Override
            public void zoomLevelChanged(boolean isZoomed) {
                if(isZoomed)
                    setViewPagerSlop(9);
                else
                    setViewPagerSlop(1);
                return;
            }
        });

    }

    private void setViewPagerSlop(int multiplier){
        int initialValue = 42;

        try {
            if(currentPagerSlopMultiplier != multiplier) {
                Field recyclerViewField = viewPager.getClass().getDeclaredField("mRecyclerView");
                recyclerViewField.setAccessible(true);
                RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);
                Field field = RecyclerView.class.getDeclaredField("mTouchSlop");
                field.setAccessible(true);
                field.set(recyclerView, initialValue * multiplier);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }finally {
            currentPagerSlopMultiplier = multiplier;
        }
    }
    private void setupNavigationControls(View view){
        topLayout = view.findViewById(R.id.topLinearLayout);
        backButton = topLayout.findViewById(R.id.backButton);
        fileName = topLayout.findViewById(R.id.file_name);
        fileNavigator = view.findViewById(R.id.fileviewer_navigator);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        fileNavigator.setNextButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
            }
        });
        fileNavigator.setPreviousButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
            }
        });
    }
}
