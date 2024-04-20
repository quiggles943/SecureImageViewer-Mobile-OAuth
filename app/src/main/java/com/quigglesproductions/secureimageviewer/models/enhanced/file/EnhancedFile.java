package com.quigglesproductions.secureimageviewer.models.enhanced.file;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class EnhancedFile implements ItemBaseModel,IDisplayFile {
    //public int id;
    @SerializedName("Id")
    public int onlineId;
    @SerializedName("EncodedName")
    public String encodedName;
    @SerializedName("NormalName")
    public String normalName;
    @SerializedName("Size")
    public long size;
    @SerializedName("Uri")
    public String onlineUri;
    @SerializedName("FolderId")
    public int onlineFolderId;
    @SerializedName("ContentType")
    public String contentType;
    @SerializedName("ThumbnailUri")
    public String onlineThumbnailUri;
    @SerializedName("AnimatedThumbnailUri")
    public String onlineAnimatedThumbnailUri;
    @SerializedName("UpdateTime")
    public LocalDateTime updateTime;
    @SerializedName("HasVarients")
    public boolean hasVarients;
    @SerializedName("Metadata")
    public FileMetadata metadata;

    transient IFileDataSource dataSource;

    public EnhancedFile(){

    }

    public int getOnlineId() {
        return onlineId;
    }

    public int getOnlineFolderId() {
        return onlineFolderId;
    }

    @Override


    public String getName() {
        return normalName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }



    @Override
    public void setWidth(int imageWidth) {
        if(metadata == null)
            return;
        metadata.width = imageWidth;
    }

    @Override
    public void setHeight(int imageHeight) {
        if(metadata == null)
            return;
        metadata.height = imageHeight;
    }

    public String getCatagoryListString() {
        if(metadata == null)
            return "";
        else {
            String categoryString = "";
            if(metadata.categories != null && metadata.categories.size()>0) {
                for (EnhancedCategory category : metadata.categories) {
                    categoryString = categoryString + category.name + ", ";
                }
                if (metadata.categories.size() > 0)
                    categoryString = categoryString.substring(0, categoryString.length() - 2);
            }
            return categoryString;
        }
    }

    public String getSubjectListString() {
        if(metadata == null)
            return "";
        else {
            String subjectString = "";
            if(metadata.subjects != null && metadata.subjects.size()>0) {
                for (EnhancedSubject subject : metadata.subjects) {
                    subjectString = subjectString + subject.name + ", ";
                }
                if (metadata.subjects.size() > 0)
                    subjectString = subjectString.substring(0, subjectString.length() - 2);
            }
            return subjectString;
        }
    }

    @Override
    public String getFileTypeString() {
        if(metadata == null)
            return "";
        return metadata.fileType;
    }

    public String getArtistName() {
        if(metadata == null)
            return "";
        else {
            if(metadata.artist == null)
                return "";
            else
                return metadata.artist.name;
        }
    }
    @NonNull
    @Override
    public FileType getFileType() {
        if(metadata == null)
            return FileType.UNKNOWN;
        if(metadata.fileExtension == null)
            return FileType.UNKNOWN;
        if(metadata.fileExtension.isEmpty())
            return FileType.UNKNOWN;
        return FileType.getFileTypeFromExtension(metadata.fileExtension);
    }

    public void setDataSource(IFileDataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public long getFolderId() {
        return onlineFolderId;
    }

    @Override
    public Long getId() {
        return (long) onlineId;
    }

    public IFileDataSource getDataSource() {
        return dataSource;
    }

    public FileMetadata getMetadata(){
        return metadata;
    }

    public LocalDateTime getImportTime(){
        if(metadata == null)
            return LocalDateTime.MIN;
        return metadata.getCreationTime();
    }

    public EnhancedArtist getArtist(){
        if(metadata == null)
            return null;
        else
            return metadata.artist;
    }

    public ArrayList<EnhancedSubject> getSubjects() {
        if(metadata == null)
            return null;
        else
            return metadata.subjects;
    }

    public ArrayList<EnhancedCategory> getCategories() {
        if(metadata == null)
            return null;
        else
            return metadata.categories;
    }

    public LocalDateTime getDefaultSortTime() {
        return metadata.getCreationTime();
    }
}
