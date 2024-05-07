package com.quigglesproductions.secureimageviewer.models.modular.folder;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IRemoteFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModularOnlineFolder extends ModularFolder implements IRemoteFolder {

    private ArrayList<IDisplayFile> files = new ArrayList<>();
    public ModularOnlineFolder(){
        super();
        //setDataSource(new OnlineFolderDataSource(this));
    }

    public void setItems(ArrayList<IDisplayFile> files) {
        this.files = files;
    }

    public void addItem(IDisplayFile item) {
        if(this.files == null){
            this.files = new ArrayList<>();
        }
        files.add(item);
    }

    public ArrayList<IDisplayFile> getItems() {
        return files;
    }

    public ArrayList<IDisplayFile> getBaseItems() {
        ArrayList<IDisplayFile> baseFiles = new ArrayList<>();
        if(files == null)
            return baseFiles;
        for(IDisplayFile file:files){
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
                files.sort(Comparator.comparing(IDisplayFile::getName));
                break;
            case NAME_DESC:
                files.sort(Comparator.comparing(IDisplayFile::getName).reversed());
                break;
            case NEWEST_FIRST:
                files.sort(Comparator.comparing(IDisplayFile::getDefaultSortTime).reversed());
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime));
                break;
            case OLDEST_FIRST:
                files.sort(Comparator.comparing(IDisplayFile::getDefaultSortTime));
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime).reversed());
                break;
            default:
                files.sort(Comparator.comparing(IDisplayFile::getName));
                break;
        }
    }

    @Override
    public FolderOrigin getFolderOrigin() {
        return FolderOrigin.ONLINE;
    }

    @Override
    public List<IDisplayFile> getFiles(){
        return new ArrayList<>(files);
    }

}
