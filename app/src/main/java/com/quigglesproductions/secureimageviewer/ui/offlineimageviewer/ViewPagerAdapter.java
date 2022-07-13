package com.quigglesproductions.secureimageviewer.ui.offlineimageviewer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.onlineimageviewer.NewTouchImageView;

import java.util.Objects;

public class ViewPagerAdapter extends PagerAdapter {

    // Context object
    Context context;

    FolderModel itemFolder;

    // Layout Inflater
    LayoutInflater mLayoutInflater;

    View.OnClickListener clickListener = null;


    // Viewpager Constructor
    public ViewPagerAdapter(Context context, FolderModel itemFolder) {
        this.context = context;
        this.itemFolder = itemFolder;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // return the number of images
        return itemFolder.getFileCount();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // inflating the item.xml
        FileModel item = itemFolder.getItemAtPosition(position);
        View itemView = mLayoutInflater.inflate(R.layout.activity_image_view, container, false);
        // referencing the image view from the item.xml file
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
        // setting the image in the imageView
        /*if(item.getIsAnimated()) {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(item.getImageFile());
                Drawable drawable = ImageDecoder.decodeDrawable(source);
                imageView.setImageDrawable(drawable);
                ((AnimatedImageDrawable) drawable).start();
            }catch(Exception e){
                Log.e("Error",e.getMessage());
            }
        }
            else*/
        try {
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
            }).load(item.getImageFile()).dontTransform().into(imageView);
        }catch(Exception ex){
            Log.e("Error", ex.getMessage());
        }
        //imageView.setImageBitmap(BitmapFactory.decodeFile(item.getImageFile().getAbsolutePath()));
        //imageView.setImageBitmap(BitmapFactory.decodeFile(itemFolder.getItemAtPosition(position).getImageFile().getAbsolutePath()));
        //Glide.with(context).load(itemFolder.getItemAtPosition(position).getImageFile()).asBitmap().into(imageView);

        // Adding the View
        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout) object);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return itemFolder.getItemAtPosition(position).getName();
    }

    public void setOnClickListener(View.OnClickListener l){
        clickListener = l;
    }
}
