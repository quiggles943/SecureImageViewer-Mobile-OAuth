package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.view.ActionProvider;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.IFileViewer;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.SupportActionBarSetListener;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;

import java.lang.reflect.Field;
@OptIn(markerClass = UnstableApi.class)
public class EnhancedFileViewFragment extends Fragment implements IFileViewer {
    private int startPos;
    ViewPager2 viewPager;
    LinearLayout topLayout;
    ImageButton backButton;
    TextView fileName;
    FileViewerNavigator fileNavigator;
    EnhancedFile selectedFile;
    int currentPagerSlopMultiplier;
    boolean hasStartPosition;
    EnhancedFileViewerViewModel viewModel;
    public EnhancedFileViewFragment(){
        hasStartPosition = false;
    }
    public EnhancedFileViewFragment(int startPosition){
        startPos = startPosition;
        hasStartPosition = true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EnhancedFileViewerViewModel.class);
        if(((EnhancedMainMenuActivity)requireActivity()).getSupportActionBar() != null) {
            ((EnhancedMainMenuActivity) requireActivity()).getSupportActionBar().hide();
        }
        else{
            ((EnhancedMainMenuActivity) requireActivity()).registerActionBarSetListener(new SupportActionBarSetListener() {
                @Override
                public void SupportActionBarSet() {
                    ((EnhancedMainMenuActivity) requireActivity()).getSupportActionBar().hide();
                }
            });
        }
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(),R.style.FileViewerTheme);
        LayoutInflater themedInflater = inflater.cloneInContext(wrapper);
        ((EnhancedMainMenuActivity)getActivity()).hideStatusBar();
        return themedInflater.inflate(R.layout.fragment_file_pager, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.fragment_view_pager);
        if(!hasStartPosition)
            startPos = EnhancedFileViewFragmentArgs.fromBundle(getArguments()).getStartPosition();
        setupNavigationControls(view);
        EnhancedFileCollectionAdapter collectionAdapter = new EnhancedFileCollectionAdapter(this);
        collectionAdapter.setFileNavigator(fileNavigator);
        EnhancedFolder selectedFolder = FolderManager.getInstance().getCurrentFolder();
        collectionAdapter.addFiles(selectedFolder.getBaseItems());
        viewPager.setAdapter(collectionAdapter);
        super.onViewCreated(view, savedInstanceState);
        hideSystemBars();
        fileNavigator.setFileTotal(collectionAdapter.getItemCount());
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
                fileNavigator.setFilePosition(position+1);
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
        collectionAdapter.setFileZoomLevelCallback(new EnhancedFileCollectionAdapter.ZoomLevelChangeCallback() {
            @Override
            public void zoomLevelChanged(boolean isZoomed) {
                if(isZoomed)
                    setViewPagerSlop(11);
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
            @SuppressLint("RestrictedApi")
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
        fileNavigator.setVisibilityChangedListener(new ActionProvider.VisibilityListener() {
            @Override
            public void onActionProviderVisibilityChanged(boolean isVisible) {
                if(isVisible)
                    topLayout.setVisibility(View.VISIBLE);
                else
                    topLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void hideSystemBars(){
        View decorView =  requireActivity().getWindow().getDecorView();
        WindowInsetsController windowInsetsController = decorView.getWindowInsetsController();
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //viewModel.getSystemBarsHidden().setValue(true);
    }

    private void showSystemBars(){
        View decorView =  requireActivity().getWindow().getDecorView();
        WindowInsetsController windowInsetsController = decorView.getWindowInsetsController();
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_DEFAULT
        );
        windowInsetsController.show(WindowInsets.Type.systemBars());
        //viewModel.getSystemBarsHidden().setValue(false);
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    }

    @Override
    public FileViewerNavigator getNavigator() {
        return fileNavigator;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        //((EnhancedMainMenuActivity)getActivity()).showStatusBar();
        //showSystemBars();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showSystemBars();
        if(((SecureActivity)requireActivity()).getSupportActionBar() != null) {
            ((SecureActivity) requireActivity()).getSupportActionBar().show();
        }

    }

}
