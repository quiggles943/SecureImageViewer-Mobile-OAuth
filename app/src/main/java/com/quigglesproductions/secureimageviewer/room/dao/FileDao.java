package com.quigglesproductions.secureimageviewer.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileSubjectCrossRef;
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException;
import com.quigglesproductions.secureimageviewer.room.exceptions.NotInDatabaseException;
import com.quigglesproductions.secureimageviewer.room.relations.FileWithMetadata;

import java.util.List;

@Dao
public abstract class FileDao {

    @Transaction
    @Query("SELECT * FROM Files")
    public abstract List<FileWithMetadata> getAll();

    @Transaction
    @Query("SELECT * FROM Files WHERE FileId IN (:fileIds)")
    public abstract List<FileWithMetadata> loadAllByIds(int[] fileIds);

    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId IN (:folderId)")
    public abstract List<FileWithMetadata> loadAllInFolder(int folderId);

    @Transaction
    @Query("SELECT * FROM Files WHERE OnlineFolderId IN (:onlineFolderId)")
    public abstract List<FileWithMetadata> loadAllInOnlineFolder(int onlineFolderId);

    @Insert
    public void insertAll(RoomDatabaseFolder folder, FileWithMetadata... files) throws DatabaseInsertionException {
        if(files != null && files.length>0){
            for(FileWithMetadata file : files){
                if(folder.getUid() == 0)
                    throw new DatabaseInsertionException(new NotInDatabaseException());
                file.file.setFolderId(folder.getUid());
                long fileId = insert(file.file);
                file.metadata.metadata.uid = fileId;

                if(file.metadata.artist != null) {
                    long artistId = insert(file.metadata.artist);
                    file.metadata.metadata.artistId = artistId;
                    file.metadata.metadata.onlineArtistId = file.metadata.artist.onlineId;
                }
                if(file.metadata.categories != null){
                    for(RoomDatabaseCategory category : file.metadata.categories){
                        long categoryId = insert(category);
                        RoomFileCategoryCrossRef categoryCrossRef = new RoomFileCategoryCrossRef();
                        categoryCrossRef.categoryId = categoryId;
                        categoryCrossRef.fileId = fileId;
                        insert(categoryCrossRef);
                    }
                }
                if(file.metadata.subjects != null)
                {
                    for(RoomDatabaseSubject subject : file.metadata.subjects){
                        long subjectId = insert(subject);
                        RoomFileSubjectCrossRef subjectCrossRef = new RoomFileSubjectCrossRef();
                        subjectCrossRef.subjectId = subjectId;
                        subjectCrossRef.fileId = fileId;
                        insert(subjectCrossRef);
                    }
                }
                insert(file.metadata.metadata);

            }
        }
    }
    @Insert
    abstract long insert(RoomDatabaseCategory category);
    @Insert
    abstract long insert(RoomDatabaseSubject subject);
    @Insert
    abstract void insert(RoomFileSubjectCrossRef subjectCrossRef);
    @Insert
    abstract void insert(RoomFileCategoryCrossRef categoryCrossRef);

    @Delete
    abstract void delete(RoomDatabaseFile file);
    @Delete
    abstract void deleteAll(RoomDatabaseFile... files);

    @Insert
    abstract long insert(RoomDatabaseFile file);
    @Insert
    abstract long insert(RoomFileMetadata metadata);
    @Insert
    abstract long insert(RoomDatabaseArtist artist);
    @Insert
    abstract void insertAll(RoomDatabaseCategory... categories);
    @Insert
    abstract void insertAll(RoomDatabaseSubject... subjects);
}
