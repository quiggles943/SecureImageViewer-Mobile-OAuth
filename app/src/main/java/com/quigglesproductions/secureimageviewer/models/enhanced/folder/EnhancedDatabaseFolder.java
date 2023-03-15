package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.LocalFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.OnlineFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnhancedDatabaseFolder extends EnhancedFolder implements ILocalFolder{
    private int id;
    private LocalDateTime accessTime;

    private File thumbnailFile;
    private File folderFile;
    private ArrayList<EnhancedDatabaseFile> files;
    public EnhancedDatabaseFolder(Context context){
        super();
        setDataSource(new LocalFolderDataSource(context,this));
    }

    public EnhancedDatabaseFolder(int id,Context context){
        super();
        this.id = id;
        setDataSource(new LocalFolderDataSource(context,this));
    }
    public LocalDateTime getAccessTime(){
        return accessTime;
    }

    public int getId(){
        return id;
    }


    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }


    public String getAccessTimeString() {
        if(accessTime == null)
            return "";
        else
            return accessTime.toString();
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setThumbnailFile(File file) {
        this.thumbnailFile = file;
    }
    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public File getFolderFile() {
        return folderFile;
    }
    public void setFolderFile(File file){
        this.folderFile = file;
    }

    public LocalDateTime getDownloadTime() {
        return LocalDateTime.now();
    }

    public void setItems(ArrayList<EnhancedDatabaseFile> files) {
        this.files = files;
    }

    public void addItem(EnhancedDatabaseFile item) {
        if(this.files == null){
            this.files = new ArrayList<>();
        }
        files.add(item);
    }

    public ArrayList<EnhancedDatabaseFile> getItems() {
        return files;
    }

    public ArrayList<EnhancedFile> getBaseItems() {
        ArrayList<EnhancedFile> baseFiles = new ArrayList<>();
        for(EnhancedDatabaseFile file:files){
            baseFiles.add(file);
        }
        return baseFiles;
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
        return FolderOrigin.LOCAL;
    }
    @Override
    public List<EnhancedFile> getFiles(){
        return new ArrayList<>(files);
    }
}
