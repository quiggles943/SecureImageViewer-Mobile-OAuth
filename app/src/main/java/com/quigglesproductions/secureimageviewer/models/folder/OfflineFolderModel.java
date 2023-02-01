package com.quigglesproductions.secureimageviewer.models.folder;

import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.file.OfflineFileModel;

import java.util.ArrayList;
import java.util.Date;

public class OfflineFolderModel extends FolderModel{
    //ArrayList<OfflineFileModel> itemList;
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

    public ArrayList<OfflineFileModel> getOfflineItems() {
        ArrayList<OfflineFileModel> files = new ArrayList<>();
        for(ItemBaseModel file : getItems())
            files.add((OfflineFileModel) file);
        return files;
    }

    public void setOfflineItems(ArrayList<OfflineFileModel> items) {
        clearItems();
        for(OfflineFileModel file : items)
            addItem(file);
    }
}
