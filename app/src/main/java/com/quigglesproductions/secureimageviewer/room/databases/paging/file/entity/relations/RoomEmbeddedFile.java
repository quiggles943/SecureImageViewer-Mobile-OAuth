package com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.datasource.file.RoomModularFileDataSource;
import com.quigglesproductions.secureimageviewer.datasource.file.RoomPagingFileDataSource;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingArtist;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingCategory;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFile;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileSubjectCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingSubject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomEmbeddedFile implements ItemBaseModel, IDatabaseFile {
    @Embedded
    public RoomPagingFile file;
    @Relation(parentColumn = "FileId",entityColumn = "FileId",entity = RoomPagingMetadata.class)
    public RoomEmbeddedMetadata metadata;
    @Relation(parentColumn = "FileId",entityColumn = "SubjectId",associateBy = @Junction(RoomPagingFileSubjectCrossRef.class))
    public List<RoomPagingSubject> subjects;
    @Relation(parentColumn = "FileId",entityColumn = "CategoryId",associateBy = @Junction(RoomPagingFileCategoryCrossRef.class))
    public List<RoomPagingCategory> categories;
    //@Relation(parentColumn = "ArtistId",entityColumn = "ArtistId")
    //public RoomModularArtist artist;
    @Ignore
    private transient IFileDataSource dataSource;
    public RoomEmbeddedFile() {
        dataSource = new RoomPagingFileDataSource(this);
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
        if(metadata == null || metadata.artist == null)
            return null;
        else
            return metadata.getArtistName();
    }

    @Override
    public String getCatagoryListString() {
        if(metadata == null)
            return "";
        else {
            String categoryString = "";
            if(categories != null && categories.size()>0) {
                for (RoomPagingCategory category : categories) {
                    categoryString = categoryString + category.name + ", ";
                }
                if (categories.size() > 0)
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
            if(subjects != null && subjects.size()>0) {
                for (RoomPagingSubject subject : subjects) {
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
        if(metadata == null|| metadata == null)
            return;
        metadata.setWidth(imageWidth);
    }

    @Override
    public void setHeight(int imageHeight) {
        if(metadata == null)
            return;
        metadata.setHeight(imageHeight);
    }
    @NonNull
    @Override
    public FileType getFileType() {
        if(metadata == null)
            return FileType.UNKNOWN;
        if(metadata.getFileExtension() == null)
            return FileType.UNKNOWN;
        if(metadata.getFileExtension().isEmpty())
            return FileType.UNKNOWN;
        return FileType.getFileTypeFromExtension(metadata.getFileExtension());
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

    public void setDownloadTime(LocalDateTime time) {
        metadata.setDownloadTime(time);
    }

    public static class Creator{
        //FileWithMetadata file = new FileWithMetadata();
        RoomPagingFile databaseFile;
        RoomEmbeddedMetadata metadata;
        RoomPagingArtist artist;
        List<RoomPagingCategory> categories;
        List<RoomPagingSubject> subjects;
        public Creator loadFromOnlineFile(ModularOnlineFile onlineFile){
            databaseFile = generateFileFromOnlineFile(onlineFile);
            metadata = generateMetadataFromOnlineFile(onlineFile.getMetadata());
            if(onlineFile.getArtist()!= null)
                artist = generateArtistFromFileTag(onlineFile.getArtist());
            categories = generateCategoryListFromFileTags(new ArrayList<>(onlineFile.categories));
            subjects = generateSubjectListFromFileTags(new ArrayList<>(onlineFile.subjects));
            return this;
        }

        public RoomEmbeddedFile build(){
            RoomEmbeddedFile fileWithMetadata = new RoomEmbeddedFile();
            fileWithMetadata.file = databaseFile;
            fileWithMetadata.metadata = metadata;
            fileWithMetadata.metadata.artist = artist;
            fileWithMetadata.categories = categories;
            fileWithMetadata.subjects = subjects;
            return fileWithMetadata;
        }

        private RoomEmbeddedMetadata generateMetadataFromOnlineFile(IFileMetadata fileMetadata) {
            RoomEmbeddedMetadata metadata = new RoomEmbeddedMetadata();
            RoomPagingMetadata roomFileMetadata = new RoomPagingMetadata();
            roomFileMetadata.width = fileMetadata.getWidth();
            roomFileMetadata.height = fileMetadata.getHeight();
            roomFileMetadata.fileSize = fileMetadata.getFileSize();
            roomFileMetadata.hasAnimatedThumbnail = fileMetadata.hasAnimatedThumbnail();
            roomFileMetadata.creationTime = fileMetadata.getCreationTime();
            roomFileMetadata.isEncrypted = fileMetadata.getIsEncrypted();
            roomFileMetadata.fileExtension = fileMetadata.getFileExtension();
            roomFileMetadata.fileType = fileMetadata.getFileType();
            roomFileMetadata.onlineFileId = fileMetadata.getOnlineFileId();
            roomFileMetadata.artistId = fileMetadata.getOnlineArtistId();
            roomFileMetadata.pageNumber = fileMetadata.getPageNumber();
            roomFileMetadata.orientation = fileMetadata.getOrientation();
            metadata.metadata = roomFileMetadata;
            return metadata;
        }

        private RoomPagingFile generateFileFromOnlineFile(ModularOnlineFile onlineFile){
            RoomPagingFile databaseFile = new RoomPagingFile();
            databaseFile.onlineId = onlineFile.onlineId;
            databaseFile.encodedName = onlineFile.encodedName;
            databaseFile.normalName = onlineFile.normalName;
            databaseFile.size = onlineFile.size;
            databaseFile.onlineFolderId = onlineFile.onlineFolderId;
            databaseFile.contentType = onlineFile.contentType;
            databaseFile.hasVarients = onlineFile.hasVarients;
            databaseFile.checksumMethod = onlineFile.checksumMethod;
            databaseFile.fileChecksum = onlineFile.checksum;
            databaseFile.createdDate = onlineFile.createdDate;
            databaseFile.retrievedDate = LocalDateTime.now();
            return databaseFile;
        }



        private RoomPagingArtist generateArtistFromFileTag(IFileTag fileTag){
            RoomPagingArtist artist = new RoomPagingArtist();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }
        private RoomPagingCategory generateCategoryFromFileTag(IFileTag fileTag){
            RoomPagingCategory artist = new RoomPagingCategory();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }

        private List<RoomPagingCategory> generateCategoryListFromFileTags(List<IFileTag>fileTags){
            List<RoomPagingCategory> categoryList = new ArrayList<>();
            for(IFileTag fileTag:fileTags){
                categoryList.add(generateCategoryFromFileTag(fileTag));
            }
            return categoryList;
        }

        private RoomPagingSubject generateSubjectFromFileTag(IFileTag fileTag){
            RoomPagingSubject artist = new RoomPagingSubject();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }

        private List<RoomPagingSubject> generateSubjectListFromFileTags(List<IFileTag>fileTags){
            List<RoomPagingSubject> subjectList = new ArrayList<>();
            for(IFileTag fileTag:fileTags){
                subjectList.add(generateSubjectFromFileTag(fileTag));
            }
            return subjectList;
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
