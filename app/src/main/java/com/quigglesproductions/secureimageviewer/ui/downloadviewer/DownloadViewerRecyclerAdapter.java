package com.quigglesproductions.secureimageviewer.ui.downloadviewer;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListRecyclerAdapter;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class DownloadViewerRecyclerAdapter extends RecyclerView.Adapter<DownloadViewerRecyclerAdapter.ViewHolder> {
    private RecyclerView recyclerView;
    private List<DownloadManager.FolderDownload> folderDownloads = new ArrayList<>();
    private ArrayList<Integer> selected = new ArrayList<>();
    private boolean multiSelect = false;
    private EnhancedFolderListRecyclerAdapter.SelectionChangedListener selectionModeChangeListener;
    private EnhancedFolderListRecyclerAdapter.FolderListRecyclerViewOnClickListener onClickListener;

    public void setOnClickListener(EnhancedFolderListRecyclerAdapter.FolderListRecyclerViewOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public DownloadManager.FolderDownload getItem(int position) {
        return folderDownloads.get(position);
    }

    public ArrayList<? extends Parcelable> getItems() {
        //return folders;
        return null;
    }

    public int getSelectedCount(){
        return selected.size();
    }
    public ArrayList<Integer> getSelectedPositions(){
        return selected;
    }
    public void addToSelected(int position) {
        selected.add(position);
        notifyItemChanged(position);
        if(selectionModeChangeListener != null)
            selectionModeChangeListener.selectionAdded(position);
    }
    public void removeFromSelected(int position){
        selected.remove(selected.indexOf(position));
        notifyItemChanged(position);
        if(selectionModeChangeListener != null)
            selectionModeChangeListener.selectionRemoved(position);
    }

    public boolean getIsSelected(int position) {
        if(selected.contains(position))
            return true;
        else
            return false;
    }
    public boolean getMultiSelect(){
        return multiSelect;
    }
    public void setMultiSelect(boolean multiSelect){
        this.multiSelect = multiSelect;
        if(selectionModeChangeListener != null) {
            if(multiSelect)
                selectionModeChangeListener.selectionModeChanged(RecyclerViewSelectionMode.MULTI);
            else {
                selectionModeChangeListener.selectionModeChanged(RecyclerViewSelectionMode.SINGLE);
                selected.clear();
                notifyDataSetChanged();

            }
        }
    }

    public void setOnSelectionModeChangeListener(EnhancedFolderListRecyclerAdapter.SelectionChangedListener selectionModeChangeListener) {
        this.selectionModeChangeListener = selectionModeChangeListener;
    }

    public void add(DownloadManager.FolderDownload folder) {
        this.folderDownloads.add(folder);
        notifyDataSetChanged();
    }

    public void setFolderDownloads(List<DownloadManager.FolderDownload> enhancedFolders) {
        this.folderDownloads = enhancedFolders;
        notifyDataSetChanged();
    }

    public List<DownloadManager.FolderDownload> getSelectedFolderDownloads() {
        List<DownloadManager.FolderDownload> result = new ArrayList<>();
        for (Integer pos : getSelectedPositions())
            result.add(getItem(pos));

        return result;
    }

    public void removeFolderDownload(DownloadManager.FolderDownload folder) {
        int position = folderDownloads.indexOf(folder);
        folderDownloads.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView downloadName;
        private final TextView downloadStatus;
        private final TextView downloadCount;
        private final TextView downloadTotal;
        private final ProgressBar progressBar;
        public ViewHolder(View view){
            super(view);
            downloadName = (TextView) view.findViewById(R.id.folderDownload_title);
            downloadStatus = (TextView) view.findViewById(R.id.folderDownload_status);
            downloadCount = (TextView) view.findViewById(R.id.folderDownload_count);
            downloadTotal = (TextView) view.findViewById(R.id.folderDownload_total);
            progressBar = (ProgressBar) view.findViewById(R.id.folderDownload_progressbar);
        }

        public TextView getDownloadName() {
            return downloadName;
        }

        public TextView getDownloadStatus() {
            return downloadStatus;
        }

        public TextView getDownloadCount() {
            return downloadCount;
        }

        public TextView getDownloadTotal() {
            return downloadTotal;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }
    }
    public DownloadViewerRecyclerAdapter(){
    }

    public DownloadViewerRecyclerAdapter(ArrayList<DownloadManager.FolderDownload> folderDownloads){
        this.folderDownloads = folderDownloads;
    }

    @Override
    public DownloadViewerRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.downloadviewer_grid_item, viewGroup, false);
        return new DownloadViewerRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull DownloadViewerRecyclerAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void onBindViewHolder(DownloadViewerRecyclerAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        DownloadManager.FolderDownload folder = folderDownloads.get(position);
        viewHolder.getDownloadName().setText(folder.getFolderName());
        viewHolder.getDownloadStatus().setText(folder.getStatus());
        viewHolder.getDownloadCount().setText(folder.getDownloadCount()+"");
        viewHolder.getDownloadTotal().setText(folder.getDownloadTotal()+"");
        viewHolder.getProgressBar().setProgress((int) (((double)folder.getDownloadCount()/(double) folder.getDownloadTotal())*100));
        viewHolder.getProgressBar().setMax(100);
        folder.setDownloadCallback(new DownloadManager.FolderDownloadCallback() {
            @Override
            public void fileDownloaded(int downloaded, int remaining) {
                viewHolder.getDownloadCount().setText(downloaded+"");
                //viewHolder.getDownloadTotal().setText(remaining+"");
                //viewHolder.getProgressBar().setProgress(downloaded);
                viewHolder.getProgressBar().setProgress((int) (((double)downloaded/(double) folder.getDownloadTotal())*100));

            }

            @Override
            public void folderDownloadComplete(DownloadManager.FolderDownload folderDownload, Exception exception) {
                if(exception == null)
                    viewHolder.getDownloadStatus().setText(folder.getStatus());
                else
                    viewHolder.getDownloadStatus().setText("Completed with errors");
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return folderDownloads.size();
    }

    public void clear(){
        folderDownloads.clear();
        notifyDataSetChanged();
    }
    public void addList(ArrayList<DownloadManager.FolderDownload> folderDownloads){
        this.folderDownloads.addAll(folderDownloads);
        notifyDataSetChanged();
    }

    public interface SelectionChangedListener {
        void selectionModeChanged(RecyclerViewSelectionMode selectionMode);
        void selectionAdded(int position);
        void selectionRemoved(int position);
    }

    public interface FolderListRecyclerViewOnClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

}
