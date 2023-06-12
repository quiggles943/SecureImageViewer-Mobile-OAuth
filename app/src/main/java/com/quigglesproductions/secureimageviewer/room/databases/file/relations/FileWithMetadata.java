package com.quigglesproductions.secureimageviewer.room.databases.file.relations;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RoomFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileMetadata;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileWithMetadata implements ItemBaseModel, IDatabaseFile {
    @Embedded
    public RoomDatabaseFile file;
    @Relation(parentColumn = "FileId",entityColumn = "FileId",entity = RoomFileMetadata.class)
    public FileMetadataWithEntities metadata;
    @Ignore
    private transient IFileDataSource dataSource;
    public FileWithMetadata() {
        dataSource = new RoomFileDataSource(this);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public LocalDateTime getDefaultSortTime() {
        return metadata.getCreationTime();
    }

    @Override
    public String getArtistName() {
        return metadata.getArtistName();
    }

    @Override
    public String getCatagoryListString() {
        if(metadata == null)
            return "";
        else {
            String categoryString = "";
            if(metadata.categories != null && metadata.categories.size()>0) {
                for (RoomDatabaseCategory category : metadata.categories) {
                    categoryString = categoryString + category.name + ", ";
                }
                if (metadata.categories.size() > 0)
                    categoryString = categoryString.substring(0, categoryString.length() - 2);
            }
            return categoryString;
        }
    }

    @Override
    public String getSubjectListString() {
        if(metadata == null)
            return "";
        else {
            String subjectString = "";
            if(metadata.subjects != null && metadata.subjects.size()>0) {
                for (RoomDatabaseSubject subject : metadata.subjects) {
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
        return metadata.getFileType();
    }

    @Override
    @Ignore
    public String getContentType() {
        //if(metadata == null|| metadata.metadata == null)
        //    return null;
        return file.contentType;
        //return metadata.metadata.contentType;
    }

    @Override
    public void setWidth(int imageWidth) {
        if(metadata == null|| metadata.metadata == null)
            return;
        metadata.metadata.width = imageWidth;
    }

    @Override
    public void setHeight(int imageHeight) {
        if(metadata == null|| metadata.metadata == null)
            return;
        metadata.metadata.height = imageHeight;
    }
    @NonNull
    @Override
    public FileType getFileType() {
        if(metadata == null|| metadata.metadata == null)
            return FileType.UNKNOWN;
        if(metadata.metadata.fileExtension == null)
            return FileType.UNKNOWN;
        if(metadata.metadata.fileExtension.isEmpty())
            return FileType.UNKNOWN;
        return FileType.getFileTypeFromExtension(metadata.metadata.fileExtension);
    }

    @Override
    public IFileMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void setDataSource(IFileDataSource retrofitFileDataSource) {
        file.setDataSource(retrofitFileDataSource);
    }

    @Override
    public long getFolderId() {
        return file.getFolderId();
    }

    @Override
    public long getId() {
        return file.getUid();
    }

    @Override
    public int getOnlineId() {
        return file.getOnlineId();
    }

    @Override
    public IFileDataSource getDataSource() {
        return dataSource;
    }

    public File getImageFile() {
        return file.getImageFile();
    }

    public File getThumbnailFile() {
        return file.getThumbnailFile();
    }

    @Override
    public String getFilePath() {
        return file.getFilePath();
    }

    @Override
    public void setFilePath(String filePath) {
        file.setFilePath(filePath);
    }

    @Override
    public String getThumbnailPath() {
        return file.getThumbnailPath();
    }

    @Override
    public void setThumbnailPath(String thumbnailPath) {
        file.setThumbnailPath(thumbnailPath);
    }

    @Override
    public void setImageFile(File file) {
        this.file.setImageFile(file);
        this.file.setFilePath(file.getAbsolutePath());
    }

    @Override
    public void setThumbnailFile(File thumbnail) {
        this.file.setThumbnailFile(thumbnail);
        this.file.setThumbnailPath(thumbnail.getAbsolutePath());
    }

    public static class Creator{
        //FileWithMetadata file = new FileWithMetadata();
        RoomDatabaseFile databaseFile;
        FileMetadataWithEntities metadataWithEntities;
        public Creator loadFromOnlineFile(EnhancedOnlineFile onlineFile){
            databaseFile = generateFileFromOnlineFile(onlineFile);
            metadataWithEntities = generateMetadataFromOnlineFile(onlineFile.getMetadata());
            return this;
        }

        public FileWithMetadata build(){
            FileWithMetadata fileWithMetadata = new FileWithMetadata();
            fileWithMetadata.file = databaseFile;
            fileWithMetadata.metadata = metadataWithEntities;
            return fileWithMetadata;
        }

        private FileMetadataWithEntities generateMetadataFromOnlineFile(IFileMetadata fileMetadata) {
            FileMetadataWithEntities metadataWithEntities = new FileMetadataWithEntities();
            RoomFileMetadata roomFileMetadata = new RoomFileMetadata();
            roomFileMetadata.width = fileMetadata.getWidth();
            roomFileMetadata.height = fileMetadata.getHeight();
            roomFileMetadata.fileSize = fileMetadata.getFileSize();
            roomFileMetadata.hasAnimatedThumbnail = fileMetadata.hasAnimatedThumbnail();
            roomFileMetadata.creationTime = fileMetadata.getCreationTime();
            roomFileMetadata.isEncrypted = fileMetadata.getIsEncrypted();
            roomFileMetadata.onlineArtistId = fileMetadata.getOnlineArtistId();
            roomFileMetadata.fileExtension = fileMetadata.getFileExtension();
            roomFileMetadata.contentType = fileMetadata.getContentType();
            roomFileMetadata.fileType = fileMetadata.getFileType();
            roomFileMetadata.onlineFileId = fileMetadata.getOnlineFileId();
            metadataWithEntities.metadata = roomFileMetadata;

            if(fileMetadata.getArtist() != null)
                metadataWithEntities.artist = generateArtistFromFileTag(fileMetadata.getArtist());

            if(fileMetadata.getSubjects() != null && fileMetadata.getSubjects().size()>0){
                List<RoomDatabaseSubject> subjectList = new ArrayList<>();
                for(IFileTag tag : fileMetadata.getSubjects()){
                    RoomDatabaseSubject subject = generateSubjectFromFileTag(tag);
                    subjectList.add(subject);
                }
                metadataWithEntities.subjects = subjectList;
            }
            if(fileMetadata.getCategories() != null && fileMetadata.getCategories().size()>0){
                List<RoomDatabaseCategory> categoryList = new ArrayList<>();
                for(IFileTag tag : fileMetadata.getCategories()){
                    RoomDatabaseCategory category = generateCategoryFromFileTag(tag);
                    categoryList.add(category);
                }
                metadataWithEntities.categories = categoryList;
            }

            return metadataWithEntities;
        }

        private RoomDatabaseFile generateFileFromOnlineFile(EnhancedOnlineFile onlineFile){
            RoomDatabaseFile databaseFile = new RoomDatabaseFile();
            databaseFile.onlineId = onlineFile.onlineId;
            databaseFile.encodedName = onlineFile.encodedName;
            databaseFile.normalName = onlineFile.normalName;
            databaseFile.size = onlineFile.size;
            databaseFile.onlineUri = onlineFile.onlineUri;
            databaseFile.onlineFolderId = onlineFile.onlineFolderId;
            databaseFile.contentType = onlineFile.contentType;
            databaseFile.onlineThumbnailUri = onlineFile.onlineThumbnailUri;
            databaseFile.onlineAnimatedThumbnailUri = onlineFile.onlineAnimatedThumbnailUri;
            databaseFile.updateTime = onlineFile.updateTime;
            databaseFile.hasVarients = onlineFile.hasVarients;
            return databaseFile;
        }



        private RoomDatabaseArtist generateArtistFromFileTag(IFileTag fileTag){
            RoomDatabaseArtist artist = new RoomDatabaseArtist();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }
        private RoomDatabaseCategory generateCategoryFromFileTag(IFileTag fileTag){
            RoomDatabaseCategory artist = new RoomDatabaseCategory();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }
        private RoomDatabaseSubject generateSubjectFromFileTag(IFileTag fileTag){
            RoomDatabaseSubject artist = new RoomDatabaseSubject();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }

        public Creator loadFromLegacyDatabaseFile(EnhancedDatabaseFile uploadFile) {
            databaseFile = generateFileFromDatabaseFile(uploadFile);
            metadataWithEntities = generateMetadataFromOnlineFile(uploadFile.getMetadata());
            return this;
        }
        private RoomDatabaseFile generateFileFromDatabaseFile(EnhancedDatabaseFile uploadFile){
            RoomDatabaseFile databaseFile = new RoomDatabaseFile();
            databaseFile.onlineId = uploadFile.onlineId;
            databaseFile.encodedName = uploadFile.encodedName;
            databaseFile.normalName = uploadFile.normalName;
            databaseFile.size = uploadFile.size;
            databaseFile.onlineUri = uploadFile.onlineUri;
            databaseFile.onlineFolderId = uploadFile.onlineFolderId;
            databaseFile.contentType = uploadFile.contentType;
            databaseFile.onlineThumbnailUri = uploadFile.onlineThumbnailUri;
            databaseFile.onlineAnimatedThumbnailUri = uploadFile.onlineAnimatedThumbnailUri;
            databaseFile.updateTime = uploadFile.updateTime;
            databaseFile.hasVarients = uploadFile.hasVarients;
            return databaseFile;
        }

    }
}
