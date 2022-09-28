package com.quigglesproductions.secureimageviewer.models.folder;

import java.util.Date;

public class OfflineFolderModel extends FolderModel{

    boolean synced;
    public OfflineFolderModel(int id, int onlineId, String name, int fileCount, Date downloadDate, Status status)
    {
        super(id,onlineId,name,fileCount,downloadDate,status);
    }

    public void setSynced(boolean synced){
        this.synced = synced;
    }

    public boolean isSynced(){
        return synced;
    }
}
