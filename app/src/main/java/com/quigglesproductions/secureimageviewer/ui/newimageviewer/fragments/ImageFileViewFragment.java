package com.quigglesproductions.secureimageviewer.ui.newimageviewer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.ui.newimageviewer.NewFileCollectionAdapter;
import com.quigglesproductions.secureimageviewer.ui.newimageviewer.NewTouchImageView;

public class ImageFileViewFragment extends BaseFileViewFragment {
    TextView fileName;
    LinearLayout topLayout,imagePagerControls;
    NewFileCollectionAdapter.ZoomLevelChangeCallback zoomLevelChangeCallback;

    public ImageFileViewFragment(NewFileCollectionAdapter.ZoomLevelChangeCallback zoomCallback){
        zoomLevelChangeCallback = zoomCallback;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_imageviewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        FileSourceType sourceType = FileSourceType.getFromKey(args.getString(ARG_FILE_SOURCE_TYPE));
        EnhancedFile file;
        switch (sourceType){
            case ONLINE:
                file = ViewerGson.getGson().fromJson(args.getString(ARG_FILE), EnhancedOnlineFile.class);
                break;
            case DATABASE:
                file = ViewerGson.getGson().fromJson(args.getString(ARG_FILE), EnhancedDatabaseFile.class);
                break;
            default:
                file = null;
                break;
        }
        loadImage(view,file);
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadImage(View itemView, EnhancedFile item){
        NewTouchImageView imageView = (NewTouchImageView) itemView.findViewById(R.id.imageViewer);
        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.onClick(v);
                }
            }
        });*/
        imageView.setMaxZoom((float)3.2);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(imageView.getCurrentZoom() == imageView.getMinZoom())
                    zoomLevelChangeCallback.zoomLevelChanged(false);
                else
                    zoomLevelChangeCallback.zoomLevelChanged(true);
                return false;
            }
        });
        try {
            IFileDataSource dataSource = item.getDataSource();
            if(dataSource == null)
                return;
            dataSource.getFullFileDataSource(new IFileDataSource.DataSourceCallback() {
                @Override
                public void FileDataSourceRetrieved(Object dataSource, Exception exception) {

                }

                @Override
                public void FileThumbnailDataSourceRetrieved(Object dataSource, Exception exception) {

                }

                @Override
                public void FileRetrievalDataSourceRetrieved(Object fileDataSource, Object fileThumbnailDataSource, Exception exception) {
                    Glide.with(getContext()).addDefaultRequestListener(new RequestListener<Object>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                            Log.e("Image Load Fail",e.getMessage());
                            e.logRootCauses("Image Load Fail");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).load(fileDataSource).thumbnail(Glide.with(getContext()).load(fileThumbnailDataSource).dontTransform()).dontTransform().into(imageView);
                }
            });

        }catch(Exception exc){
            Log.e("Error", exc.getMessage());
        }

        //imageView.setImageBitmap(BitmapFactory.decodeFile(item.getImageFile().getAbsolutePath()));
        //imageView.setImageBitmap(BitmapFactory.decodeFile(itemFolder.getItemAtPosition(position).getImageFile().getAbsolutePath()));
        //Glide.with(context).load(itemFolder.getItemAtPosition(position).getImageFile()).asBitmap().into(imageView);

        // Adding the View
    }

    private void setupControls(View view){
        View.OnClickListener prevFileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Update view to show previous file
            }
        };
        View.OnClickListener nextFileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Update view to show next file
            }
        };
        fileName = view.findViewById(R.id.file_name);
        topLayout = view.findViewById(R.id.topLinearLayout);
        //bottomLayout = view.findViewById(R.id.imageviewer_pager_layout);
        //fileViewerNavigatorImage = view.findViewById(R.id.fileviewer_navigator);
        imagePagerControls = view.findViewById(R.id.image_pager_controls);
        //setupImageNavigatorControls();


        ImageButton backButton = topLayout.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        //imageCountText = findViewById(R.id.imagecount);
        //imageTotalText = findViewById(R.id.imagetotal);

    }
}
