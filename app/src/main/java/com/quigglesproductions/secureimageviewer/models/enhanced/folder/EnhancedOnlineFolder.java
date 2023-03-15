package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.OnlineFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EnhancedOnlineFolder extends EnhancedFolder implements IRemoteFolder{

    private ArrayList<EnhancedOnlineFile> files = new ArrayList<>();
    public EnhancedOnlineFolder(){
        super();
        setDataSource(new OnlineFolderDataSource(this));
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
        if(files == null)
            return baseFiles;
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

    @Override
    public void sortFiles(SortType newSortType) {
        switch (newSortType){
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
    }

    @Override
    public FolderOrigin getFolderOrigin() {
        return FolderOrigin.ONLINE;
    }

    @Override
    public List<EnhancedFile> getFiles(){
        return new ArrayList<>(files);
    }
}
