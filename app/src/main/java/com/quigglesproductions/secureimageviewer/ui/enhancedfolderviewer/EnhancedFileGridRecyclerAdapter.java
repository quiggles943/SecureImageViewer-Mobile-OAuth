package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;

import org.acra.ACRA;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnhancedFileGridRecyclerAdapter<T extends IDisplayFile> extends RecyclerView.Adapter<EnhancedFileGridRecyclerAdapter.ViewHolder> {
    private ArrayList<T> files = new ArrayList<>();
    private Context mContext;
    private EnhancedRecyclerViewOnClickListener onClickListener;

    public void setFiles(ArrayList<T> enhancedFiles) {
        files = enhancedFiles;
        notifyDataSetChanged();
    }

    public T get(int position) {
        return files.get(position);
    }

    public void addFiles(List<T> files) {
        for (T file: files) {
            this.files.add(file);
        }
        notifyDataSetChanged();
    }
    private ArrayList<T> sortFiles(ArrayList<T> files, SortType sortType){
        switch (sortType){
            case NAME_ASC:
                files.sort(Comparator.comparing(T::getName));
                break;
            case NAME_DESC:
                files.sort(Comparator.comparing(T::getName).reversed());
                break;
            case NEWEST_FIRST:
                files.sort(Comparator.comparing(T::getDefaultSortTime).reversed());
                break;
            case OLDEST_FIRST:
                files.sort(Comparator.comparing(T::getDefaultSortTime));
                break;
        }
        return files;
    }

    public void sort(SortType sortType) {
        files = sortFiles(files,sortType);
        notifyDataSetChanged();
    }

    public List<T> getFiles() {
        return files;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            view.setOnCreateContextMenuListener(this);
        }

        public ImageView getImageView() {
            return imageView;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }

    public EnhancedFileGridRecyclerAdapter(Context context){
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.filegrid_layout_constrained, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        T file = files.get(position);
        viewHolder.getImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(viewHolder.getAdapterPosition());
            }
        });
        viewHolder.getImageView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        viewHolder.getImageView().setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menuInfo = new AdapterView.AdapterContextMenuInfo(viewHolder.itemView,viewHolder.getPosition(),0);
                onClickListener.onCreateContextMenu(menu,v,menuInfo);
            }
        });
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        try {
            file.getDataSource().getFileThumbnailDataSource(mContext,new IFileDataSource.DataSourceCallback() {
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
                    }).load(dataSource).into(viewHolder.getImageView()).clearOnDetach();
                }

                @Override
                public void FileRetrievalDataSourceRetrieved(Object fileDataSource, Object fileThumbnailDataSource, Exception exception) {

                }
            });
        } catch (MalformedURLException e) {
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return files.size();
    }

    public void setOnClickListener(EnhancedRecyclerViewOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface EnhancedRecyclerViewOnClickListener{
        void onClick(int position);

        void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
    }
}
