package com.quigglesproductions.secureimageviewer.models.file;

import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;

import java.io.File;
import java.util.Date;

public class StreamingFileModel extends FileModel implements ItemBaseModel {

    public StreamingFileModel(){

    }

    public StreamingFileModel(String name, String base64Name) {
        super(name, base64Name);
    }

    public StreamingFileModel(int itemId, int onlineId, String name, String base64Name, int artistId, int folderId, int onlineFolderId) {
        super(itemId, onlineId, name, base64Name, artistId, folderId, onlineFolderId);
    }

    public StreamingFileModel(int itemId, int onlineId, String name, String base64Name, int artistId, int folderId, int onlineFolderId, int width, int height, File imageFile, File thumbnailFile, Date downloadTime) {
        super(itemId, onlineId, name, base64Name, artistId, folderId, onlineFolderId, width, height, imageFile, thumbnailFile, downloadTime);
    }

    @Override
    public IFileDataSource getDataSource() {
            return super.getDataSource();
    }
}
