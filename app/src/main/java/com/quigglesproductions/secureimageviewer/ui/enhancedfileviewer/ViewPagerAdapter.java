package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.ui.PlayerView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.listeners.AdapterInstantiatedListener;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;

import java.util.ArrayList;
import java.util.Objects;

public class ViewPagerAdapter extends PagerAdapter {

    // Context object
    Context context;

    ArrayList<EnhancedFile> files = new ArrayList<>();
    // Layout Inflater
    LayoutInflater mLayoutInflater;
    boolean instantiated = false;
    View.OnClickListener clickListener = null;
    AdapterInstantiatedListener instantiatedListener;


    // Viewpager Constructor
    public ViewPagerAdapter(Context context) {
        this.context = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addFiles(ArrayList<EnhancedFile> fileModels){
        files.clear();
        files.addAll(fileModels);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        // return the number of images
        return files.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // inflating the item.xml
        EnhancedFile item = files.get(position);
        //View itemView = mLayoutInflater.inflate(R.layout.activity_image_view, container, false);
        View itemView;
        // referencing the image view from the item.xml file
        switch(item.metadata.fileType){
            case "IMAGE":
                itemView = mLayoutInflater.inflate(R.layout.activity_imageviewer, container, false);
                loadImage(itemView,item);
                break;
            case "VIDEO":
                itemView = mLayoutInflater.inflate(R.layout.activity_video_view, container, false);
                loadVideo(itemView,item);
                break;
            default:
                itemView = mLayoutInflater.inflate(R.layout.activity_image_view, container, false);
                loadImage(itemView,item);
                break;
        }
        itemView.setTag(position);
        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }
    private void loadVideo(View itemView,EnhancedFile item){

        PlayerView videoView = itemView.findViewById(R.id.videoView);
        //videoView.requestFocus();
        //videoView.start();
    }
    private void loadImage(View itemView,EnhancedFile item){
        NewTouchImageView imageView = (NewTouchImageView) itemView.findViewById(R.id.imageViewer);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.onClick(v);
                }
            }
        });
        imageView.setMaxZoom((float)3.2);

        try {
            IFileDataSource dataSource = item.getDataSource();
            if(dataSource == null)
                return;
            dataSource.getFullFileDataSource(context,new IFileDataSource.DataSourceCallback() {
                @Override
                public void FileDataSourceRetrieved(Object dataSource, Exception exception) {

                }

                @Override
                public void FileThumbnailDataSourceRetrieved(Object dataSource, Exception exception) {

                }

                @Override
                public void FileRetrievalDataSourceRetrieved(Object fileDataSource, Object fileThumbnailDataSource, Exception exception) {
                    Glide.with(context).addDefaultRequestListener(new RequestListener<Object>() {
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
                    }).load(fileDataSource).thumbnail(Glide.with(context).load(fileThumbnailDataSource).dontTransform()).dontTransform().into(imageView);
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

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout) object);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return files.get(position).getName();
    }

    public void setOnClickListener(View.OnClickListener l){
        clickListener = l;
    }

    public EnhancedFile getItem(int position) {
        return files.get(position);
    }

    public ArrayList<EnhancedFile> getItems() {
        return files;
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        super.finishUpdate(container);
        if(!instantiated) {
            instantiated = true;
            if(instantiatedListener != null)
                instantiatedListener.onAdapterInstantiated();
        }
    }

    public void setAdapterInstantiatedListener(AdapterInstantiatedListener listener){
        instantiatedListener = listener;
    }
}
