package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.OnlineFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;

import java.util.ArrayList;

public class EnhancedOnlineFolder extends EnhancedFolder{

    private ArrayList<EnhancedOnlineFile> files = new ArrayList<>();
    public EnhancedOnlineFolder(Context context){
        super();
        setDataSource(new OnlineFolderDataSource(context,this));
    }

    public void setItems(ArrayList<EnhancedOnlineFile> files) {
        this.files = files;
    }

    public void addItem(EnhancedOnlineFile item) {
        if(this.files == null){
            this.files = new ArrayList<>();
        }
        files.add(item);
    }

    public ArrayList<EnhancedOnlineFile> getItems() {
        return files;
    }

    public ArrayList<EnhancedFile> getBaseItems() {
        ArrayList<EnhancedFile> baseFiles = new ArrayList<>();
        for(EnhancedOnlineFile file:files){
            baseFiles.add(file);
        }
        return baseFiles;
    }

    public void setBaseItems(ArrayList<EnhancedFile> baseFiles) {
        if(files == null)
            files = new ArrayList<>();
        files.clear();
        for(EnhancedFile file : baseFiles){
            files.add((EnhancedOnlineFile) file);
        }

    }

    @Override
    public void clearItems() {
        if(files != null)
            files.clear();
    }
}
