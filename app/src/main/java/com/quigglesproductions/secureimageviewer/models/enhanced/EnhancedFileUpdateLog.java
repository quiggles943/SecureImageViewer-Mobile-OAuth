package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class EnhancedFileUpdateLog {

    @SerializedName("FileId")
    private long fileId;
    @SerializedName("FolderId")
    private long folderId;
    @SerializedName("UpdateTime")
    private LocalDateTime updateTime;

    @SerializedName("Type")
    private String type;
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public long getFileId() {
        return fileId;
    }

    public long getFolderId() {
        return folderId;
    }
    public String getTypeString(){
        return type;
    }
    public UpdateType getUpdateType(){
        return UpdateType.getUpdateType(type);
    }

    public enum UpdateType{
        Unknown,
        Created,
        Modified,
        Deleted;

        public static UpdateType getUpdateType(String value){
            UpdateType result = Unknown;
            for(UpdateType type :UpdateType.values()){
                if(type.name().equalsIgnoreCase(value))
                    result = type;
            }
            return result;
        }
    }
}
