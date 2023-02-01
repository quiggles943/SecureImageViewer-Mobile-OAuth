package com.quigglesproductions.secureimageviewer.models.file;

import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;

import java.io.File;
import java.util.Date;

public class OfflineFileModel extends FileModel{
    boolean isSynced;
    public OfflineFileModel(String name, String base64Name) {
        super(name, base64Name);
    }

    public OfflineFileModel(int itemId, int onlineId, String name, String base64Name, int artistId, int folderId, int onlineFolderId) {
        super(itemId, onlineId, name, base64Name, artistId, folderId, onlineFolderId);
        //setDataSource(new LocalDataSource(this));
    }

    public OfflineFileModel(int itemId, int onlineId, String name, String base64Name, int artistId, int folderId, int onlineFolderId, int width, int height, File imageFile, File thumbnailFile, Date downloadTime) {
        super(itemId, onlineId, name, base64Name, artistId, folderId, onlineFolderId, width, height, imageFile, thumbnailFile, downloadTime);
        //setDataSource(new LocalDataSource(this));
    }

    @Override
    public IFileDataSource getDataSource() {
        //if(super.getDataSource() == null)
            //return new LocalDataSource(this);
        //else
            return super.getDataSource();
    }
}
