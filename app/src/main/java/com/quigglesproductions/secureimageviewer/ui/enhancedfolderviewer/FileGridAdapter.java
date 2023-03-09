package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;

import org.acra.ACRA;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileGridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<EnhancedFile> items;
    private int folderId;
    private boolean isEncrypted;
    private SortType sortType = SortType.NAME_ASC;
    public FileGridAdapter(Context c, List<EnhancedFile> files)
    {
        mContext = c;
        this.items = (ArrayList<EnhancedFile>) files;
    }
    public FileGridAdapter(Context c, List<EnhancedFile> files, SortType initialSort)
    {
        mContext = c;
        this.items = sortFiles((ArrayList<EnhancedFile>) files,initialSort);
    }
    public ArrayList<EnhancedFile> sortFiles(ArrayList<EnhancedFile> files, SortType sortType){
        switch (sortType){
            case NAME_ASC:
                files.sort(Comparator.comparing(EnhancedFile::getName));
                break;
            case NAME_DESC:
                files.sort(Comparator.comparing(EnhancedFile::getName).reversed());
                break;
            case NEWEST_FIRST:
                files.sort(Comparator.comparing(EnhancedFile::getImportTime).reversed());
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime));
                break;
            case OLDEST_FIRST:
                files.sort(Comparator.comparing(EnhancedFile::getImportTime));
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime).reversed());
                break;
            default:
                files.sort(Comparator.comparing(EnhancedFile::getName));
                break;
        }
        return files;
    }

    public void add(EnhancedDatabaseFile item){
        items.add(item);
    }

    @Override
    public int getCount()
    {
        return items.size();
    }
    @Override
    public EnhancedFile getItem(int position)
    {
        return items.get(position);
    }
    @Override
    public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        EnhancedFile item = items.get(position);
        View gridView = convertView;
        if (gridView == null)
        {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.filegrid_layout_constrained, null);
            ImageView imageView = (ImageView)gridView.findViewById(R.id.grid_item_image);
            gridView.setTag(imageView);
        }
        ImageView imageView = (ImageView) gridView.getTag();
        try {
            item.getDataSource().getFileThumbnailDataSource(new IFileDataSource.DataSourceCallback() {
                @Override
                public void FileDataSourceRetrieved(Object dataSource, Exception exception) {

                }

                @Override
                public void FileThumbnailDataSourceRetrieved(Object dataSource, Exception exception) {
                    Glide.with(mContext).addDefaultRequestListener(new RequestListener<Object>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                            Log.e("Image Load Fail", e.getMessage());
                            e.logRootCauses("Image Load Fail");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).load(dataSource).into(imageView).clearOnDetach();
                }

                @Override
                public void FileRetrievalDataSourceRetrieved(Object fileDataSource, Object fileThumbnailDataSource, Exception exception) {

                }
            });
        } catch (MalformedURLException e) {
            imageView.setImageBitmap(null);
            ACRA.getErrorReporter().handleSilentException(e);
        }
        return gridView;
    }
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void sort(SortType sortType) {
        sortFiles(items,sortType);
        notifyDataSetChanged();
    }

    public void setFiles(ArrayList<EnhancedFile> enhancedFiles) {
        this.items = sortFiles(enhancedFiles,sortType);
        notifyDataSetChanged();
    }
}
