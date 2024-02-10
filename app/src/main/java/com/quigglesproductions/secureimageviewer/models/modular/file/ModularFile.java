package com.quigglesproductions.secureimageviewer.models.modular.file;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.modular.ModularArtist;
import com.quigglesproductions.secureimageviewer.models.modular.ModularCategory;
import com.quigglesproductions.secureimageviewer.models.modular.ModularFileMetadata;
import com.quigglesproductions.secureimageviewer.models.modular.ModularSubject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModularFile implements ItemBaseModel, IDisplayFile {
    //public int id;
    @SerializedName("Id")
    public int onlineId;
    @SerializedName("EncodedName")
    public String encodedName;
    @SerializedName("NormalName")
    public String normalName;
    @SerializedName("Size")
    public long size;
    @SerializedName("FolderId")
    public int onlineFolderId;
    @SerializedName("ContentType")
    public String contentType;
    @SerializedName("FileChecksum")
    public String checksum;
    @SerializedName("ChecksumMethod")
    public String checksumMethod;

    @SerializedName("UpdateTime")
    public LocalDateTime updateTime;
    @SerializedName("Varients")
    public List<ModularFile> varients;
    @SerializedName("HasVarients")
    public boolean hasVarients;
    @SerializedName("Metadata")
    public ModularFileMetadata metadata;
    @SerializedName("Artist")
    public ModularArtist artist;
    @SerializedName("Categories")
    public ArrayList<ModularCategory> categories;
    @SerializedName("Subjects")
    public ArrayList<ModularSubject> subjects;
    @SerializedName("CreatedDate")
    public LocalDateTime createdDate;

    transient IFileDataSource dataSource;

    public ModularFile(){

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
            if(categories != null && categories.size()>0) {
                for (ModularCategory category : categories) {
                    categoryString = categoryString + category.name + ", ";
                }
                if (categories.size() > 0)
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
            if(subjects != null && subjects.size()>0) {
                for (ModularSubject subject : subjects) {
                    subjectString = subjectString + subject.name + ", ";
                }
                if (subjects.size() > 0)
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
    public long getId() {
        return onlineId;
    }

    public IFileDataSource getDataSource() {
        return dataSource;
    }

    public ModularFileMetadata getMetadata(){
        return metadata;
    }

    public LocalDateTime getImportTime(){
        if(metadata == null)
            return LocalDateTime.MIN;
        return metadata.getCreationTime();
    }

    public ModularArtist getArtist(){
        if(metadata == null)
            return null;
        else
            return metadata.artist;
    }

    public ArrayList<ModularSubject> getSubjects() {
        if(metadata == null)
            return null;
        else
            return subjects;
    }

    public ArrayList<ModularCategory> getCategories() {
        if(metadata == null)
            return null;
        else
            return categories;
    }

    public LocalDateTime getDefaultSortTime() {
        return metadata.getCreationTime();
    }
}
