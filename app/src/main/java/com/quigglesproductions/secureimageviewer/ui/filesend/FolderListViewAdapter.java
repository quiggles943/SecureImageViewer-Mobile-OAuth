package com.quigglesproductions.secureimageviewer.ui.filesend;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFolder;

import java.util.ArrayList;
import java.util.List;

public class FolderListViewAdapter extends BaseAdapter {
    private List<RoomDatabaseFolder> folders = new ArrayList<>();
    private Context context;
    public FolderListViewAdapter(Context context){
        this.context = context;
    }

    public void addFolder(RoomDatabaseFolder folderModel){
        folders.add(folderModel);
    }
    public void setFolders(List<RoomDatabaseFolder> folders){
        this.folders = folders;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public RoomDatabaseFolder getItem(int position) {
        return folders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RoomDatabaseFolder item = folders.get(position);
        View gridView = convertView;
        if (gridView == null)
        {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.folderlistview_layout, null);
        }
        FolderViewModel model = new FolderViewModel(gridView);
        model.textView.setText(item.getName());
        if(item instanceof IDatabaseFolder) {
            IDatabaseFolder databaseFolder = (IDatabaseFolder) item;
            if (databaseFolder.getThumbnailFile() != null && databaseFolder.getThumbnailFile().exists()) {
                Glide.with(context).addDefaultRequestListener(new RequestListener<Object>() {
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
                }).load(databaseFolder.getThumbnailFile()).signature(new ObjectKey(databaseFolder.getDownloadTime())).into(model.getImageView()).clearOnDetach();
            } else
                model.getImageView().setImageBitmap(null);
        }
        else
        {
            model.getImageView().setImageBitmap(null);
        }
        return gridView;
    }

    private class FolderViewModel{
        private final ImageView imageView;
        private final TextView textView;
        public FolderViewModel(View view){
            textView = view.findViewById(R.id.folderlistview_foldername);
            imageView = (ImageView) view.findViewById(R.id.folderlistview_image);
        }
        public ImageView getImageView(){
            return imageView;
        }
        public TextView getTextView(){
            return textView;
        }
    }
}
