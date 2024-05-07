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

    @SerializedName("UpdateType")
    private String updateType;
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
        return updateType;
    }
    public UpdateType getUpdateType(){
        return UpdateType.getUpdateType(updateType);
    }

    public enum UpdateType{
        UNKNOWN,
        ADD,
        UPDATE,
        DELETE;

        public static UpdateType getUpdateType(String value){
            UpdateType result = UNKNOWN;
            for(UpdateType type :UpdateType.values()){
                if(type.name().equalsIgnoreCase(value))
                    result = type;
            }
            return result;
        }
    }
}
