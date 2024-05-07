package com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.checksum.ChecksumAlgorithm;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.datasource.file.RoomPagingFileDataSource;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedArtist;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedCategory;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFile;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFileSubjectCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSubject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomUnifiedEmbeddedFile implements ItemBaseModel, IDatabaseFile {
    @Embedded
    public RoomUnifiedFile file;
    @Relation(parentColumn = "FileId",entityColumn = "FileId",entity = RoomUnifiedMetadata.class)
    public RoomUnifiedEmbeddedMetadata metadata;
    @Relation(parentColumn = "FileId",entityColumn = "SubjectId",associateBy = @Junction(RoomUnifiedFileSubjectCrossRef.class))
    public List<RoomUnifiedSubject> subjects;
    @Relation(parentColumn = "FileId",entityColumn = "CategoryId",associateBy = @Junction(RoomUnifiedFileCategoryCrossRef.class))
    public List<RoomUnifiedCategory> categories;
    @Ignore
    private transient IFileDataSource dataSource;
    public RoomUnifiedEmbeddedFile() {
        dataSource = new RoomPagingFileDataSource(this);
    }


    @Override
    public String getName() {
        return file.name;
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
                for (RoomUnifiedCategory category : categories) {
                    categoryString = categoryString + category.getName() + ", ";
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
                for (RoomUnifiedSubject subject : subjects) {
                    subjectString = subjectString + subject.normalName + ", ";
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
        file.dataSource = retrofitFileDataSource;
    }

    @Override
    public long getFolderId() {
        return file.getFolderId();
    }

    @Override
    public Long getId() {
        return file.getUid();
    }

    @Override
    public int getOnlineId() {
        return file.onlineId;
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
        return file.filePath;
    }

    @Override
    public void setFilePath(String filePath) {
        file.filePath = filePath;
    }

    @Override
    public String getThumbnailPath() {
        return file.thumbnailPath;
    }

    @Override
    public void setThumbnailPath(String thumbnailPath) {
        file.thumbnailPath = thumbnailPath;
    }

    @Override
    public void setImageFile(File file) {
        this.file.setImageFile(file);
        this.file.filePath = file.getAbsolutePath();
    }

    @Override
    public void setThumbnailFile(File thumbnail) {
        this.file.setThumbnailFile(thumbnail);
        this.file.thumbnailPath = thumbnail.getAbsolutePath();
    }

    public void setDownloadTime(LocalDateTime time) {
        metadata.setDownloadTime(time);
        file.downloadedDate = time;
    }

    public static class Creator{
        //FileWithMetadata file = new FileWithMetadata();
        RoomUnifiedFile databaseFile;
        String folderName;
        RoomUnifiedEmbeddedMetadata metadata;
        RoomUnifiedArtist artist;
        List<RoomUnifiedCategory> categories;
        List<RoomUnifiedSubject> subjects;
        public Creator loadFromOnlineFile(ModularOnlineFile onlineFile){
            databaseFile = generateFileFromOnlineFile(onlineFile);
            metadata = generateMetadataFromOnlineFile(onlineFile.getMetadata());
            if(onlineFile.getArtist()!= null)
                artist = generateArtistFromFileTag(onlineFile.getArtist());
            categories = generateCategoryListFromFileTags(new ArrayList<>(onlineFile.categories));
            subjects = generateSubjectListFromFileTags(new ArrayList<>(onlineFile.subjects));
            return this;
        }

        public Creator withFolder(IDisplayFolder folder){
            folderName = folder.getName();
            return this;
        }

        public RoomUnifiedEmbeddedFile build(){
            if(folderName != null)
                databaseFile.cachedFolderName = folderName;
            RoomUnifiedEmbeddedFile fileWithMetadata = new RoomUnifiedEmbeddedFile();
            fileWithMetadata.file = databaseFile;
            fileWithMetadata.metadata = metadata;
            fileWithMetadata.metadata.artist = artist;
            fileWithMetadata.categories = categories;
            fileWithMetadata.subjects = subjects;
            return fileWithMetadata;
        }

        private RoomUnifiedEmbeddedMetadata generateMetadataFromOnlineFile(IFileMetadata fileMetadata) {
            RoomUnifiedEmbeddedMetadata metadata = new RoomUnifiedEmbeddedMetadata();
            RoomUnifiedMetadata roomFileMetadata = new RoomUnifiedMetadata();
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

        private RoomUnifiedFile generateFileFromOnlineFile(ModularOnlineFile onlineFile){
            RoomUnifiedFile databaseFile = new RoomUnifiedFile();
            databaseFile.onlineId = onlineFile.onlineId;
            databaseFile.encodedName = onlineFile.encodedName;
            databaseFile.name = onlineFile.normalName;
            databaseFile.size = onlineFile.size;
            databaseFile.onlineFolderId = onlineFile.onlineFolderId;
            databaseFile.contentType = onlineFile.contentType;
            databaseFile.hasVarients = onlineFile.hasVarients;
            databaseFile.checksumMethod = ChecksumAlgorithm.getAlgorithmFromString(onlineFile.checksumMethod);
            databaseFile.checksumString = onlineFile.checksum;
            databaseFile.createdDate = onlineFile.createdDate;
            databaseFile.retrievedDate = LocalDateTime.now();
            return databaseFile;
        }



        private RoomUnifiedArtist generateArtistFromFileTag(IFileTag fileTag){
            RoomUnifiedArtist artist = new RoomUnifiedArtist();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }
        private RoomUnifiedCategory generateCategoryFromFileTag(IFileTag fileTag){
            RoomUnifiedCategory artist = new RoomUnifiedCategory();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }

        private List<RoomUnifiedCategory> generateCategoryListFromFileTags(List<IFileTag>fileTags){
            List<RoomUnifiedCategory> categoryList = new ArrayList<>();
            for(IFileTag fileTag:fileTags){
                categoryList.add(generateCategoryFromFileTag(fileTag));
            }
            return categoryList;
        }

        private RoomUnifiedSubject generateSubjectFromFileTag(IFileTag fileTag){
            RoomUnifiedSubject artist = new RoomUnifiedSubject();
            artist.setName(fileTag.getName());
            artist.setOnlineId(fileTag.getOnlineId());
            return artist;
        }

        private List<RoomUnifiedSubject> generateSubjectListFromFileTags(List<IFileTag>fileTags){
            List<RoomUnifiedSubject> subjectList = new ArrayList<>();
            for(IFileTag fileTag:fileTags){
                subjectList.add(generateSubjectFromFileTag(fileTag));
            }
            return subjectList;
        }

        /*private RoomDatabaseFile generateFileFromDatabaseFile(EnhancedDatabaseFile uploadFile){
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
        }*/

    }
}
