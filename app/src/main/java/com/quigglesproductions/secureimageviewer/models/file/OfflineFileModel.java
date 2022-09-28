package com.quigglesproductions.secureimageviewer.models.file;

import java.io.File;
import java.util.Date;

public class OfflineFileModel extends FileModel{
    boolean isSynced;
    public OfflineFileModel(String name, String base64Name) {
        super(name, base64Name);
    }

    public OfflineFileModel(int itemId, int onlineId, String name, String base64Name, int artistId, int folderId, int onlineFolderId) {
        super(itemId, onlineId, name, base64Name, artistId, folderId, onlineFolderId);
    }

    public OfflineFileModel(int itemId, int onlineId, String name, String base64Name, int artistId, int folderId, int onlineFolderId, int width, int height, File imageFile, File thumbnailFile, Date downloadTime) {
        super(itemId, onlineId, name, base64Name, artistId, folderId, onlineFolderId, width, height, imageFile, thumbnailFile, downloadTime);
    }
}
