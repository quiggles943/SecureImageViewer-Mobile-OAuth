package com.quigglesproductions.secureimageviewer.room.databases.file.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileSubjectCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileMetadataWithEntities;
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException;
import com.quigglesproductions.secureimageviewer.room.exceptions.NotInDatabaseException;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;

import java.util.List;

@Dao
public abstract class FileDao {

    /**
     * Retrieve all files stored within the database
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files")
    public abstract List<FileWithMetadata> getAll();

    /**
     * Retrieves the files identified by the provided array of file ids
     * @param fileIds an array of file ids
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FileId IN (:fileIds)")
    public abstract List<FileWithMetadata> loadAllByIds(int[] fileIds);

    /**
     * Retrieves all the files which are part of the folder identifed by the provided folder id
     * @param folderId the id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId IN (:folderId)")
    public abstract List<FileWithMetadata> loadAllInFolder(int folderId);

    /**
     * Retrieves all the files which are part of the folder identifed by the provided online folder
     * id
     * @param onlineFolderId the online id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE OnlineFolderId IN (:onlineFolderId)")
    public abstract List<FileWithMetadata> loadAllInOnlineFolder(int onlineFolderId);

    /**
     * Inserts the provided file into the database and links it to the provided folder. If any
     * subjects or categories are listed, or if the artist is specified, they will be either added
     * or updated to the database in their respective tables and linked to this file through a many
     * to many relationship. If the file already exists on the device then it will be updated to match
     * the file provided
     * @param folder The folder to link the file to
     * @param file The file to insert to the database
     * @return the uid of the inserted file
     * @throws DatabaseInsertionException
     */
    @Transaction
    @Insert
    public long insert(RoomDatabaseFolder folder,FileWithMetadata file)throws DatabaseInsertionException{
        if(file != null){
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
            return fileId;
        }
        return 0;
    }

    /**
     * Inserts the provided files into the database and links it to the provided folder. If any
     * subjects or categories are listed, or if the artist is specified, they will be either added
     * or updated to the database in their respective tables and linked to this file through a many
     * to many relationship. If the file already exists on the device then it will be updated to match
     * the file provided
     * @param folder The folder to link the file to
     * @param files The files to insert to the database
     * @throws DatabaseInsertionException
     */
    @Transaction
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

    /**
     * Inserts the provided artist into the database. If the artist already exists then it updates
     * the existing artist with the details of the provided one
     * @param artist the artist to insert to the database
     * @return the uid of the artist
     */
    @Insert
    long insert(RoomDatabaseArtist artist){
        RoomDatabaseArtist existingArtist = getArtistByOnlineId(artist.onlineId);
        if(existingArtist != null){
            artist.uid = existingArtist.getUid();
            _update(artist);
            return existingArtist.getUid();
        }
        else{
            return _insert(artist);
        }
    }
    @Insert
    abstract long _insert(RoomDatabaseArtist category);
    @Query("SELECT * FROM Artists WHERE onlineId = :onlineId")
    abstract RoomDatabaseArtist getArtistByOnlineId(long onlineId);
    @Update
    abstract void _update(RoomDatabaseArtist category);
    @Insert
    long insert(RoomDatabaseCategory category){
        RoomDatabaseCategory existingCategory = getCategoryByOnlineId(category.onlineId);
        if(existingCategory != null){
            category.uid = existingCategory.getUid();
            _update(category);
            return existingCategory.getUid();
        }
        else{
            return _insert(category);
        }
    }
    @Insert
    abstract long _insert(RoomDatabaseCategory category);
    @Query("SELECT * FROM Categories WHERE onlineId = :onlineId")
    abstract RoomDatabaseCategory getCategoryByOnlineId(long onlineId);
    @Update
    abstract void _update(RoomDatabaseCategory category);
    @Insert
    long insert(RoomDatabaseSubject subject){
        RoomDatabaseSubject existingSubject = getSubjectByOnlineId(subject.onlineId);
        if(existingSubject != null){
            subject.uid = existingSubject.getUid();
            _update(subject);
            return existingSubject.getUid();
        }
        else{
            return _insert(subject);
        }
    }
    @Insert
    abstract long _insert(RoomDatabaseSubject category);
    @Query("SELECT * FROM Subjects WHERE onlineId = :onlineId")
    abstract RoomDatabaseSubject getSubjectByOnlineId(long onlineId);
    @Update
    public abstract void update(RoomDatabaseFile file);
    @Update
    abstract void _update(RoomDatabaseSubject category);
    @Insert
    abstract void insert(RoomFileSubjectCrossRef subjectCrossRef);
    @Insert
    abstract void insert(RoomFileCategoryCrossRef categoryCrossRef);

    @Delete
    abstract void delete(RoomDatabaseFile file);
    @Delete
    void delete(FileMetadataWithEntities file){
        if(file == null)
            return;

        deleteCategoryReferences(file.getFileId());
        deleteSubjectReferences(file.getFileId());

        delete(file.metadata);
    }
    @Delete
    abstract void delete(RoomFileMetadata metadata);
    @Query("DELETE FROM FileCategoryCrossRef WHERE FileId = :fileId")
    abstract void deleteCategoryReferences(long fileId);
    @Query("DELETE FROM FileSubjectCrossRef WHERE FileId = :fileId")
    abstract void deleteSubjectReferences(long fileId);
    @Delete
    abstract void deleteAll(RoomDatabaseFile... files);

    @Insert
    abstract long insert(RoomDatabaseFile file);
    @Insert
    abstract long insert(RoomFileMetadata metadata);
    @Insert
    abstract void insertAll(RoomDatabaseCategory... categories);
    @Insert
    abstract void insertAll(RoomDatabaseSubject... subjects);
    public void delete(FileWithMetadata file){
        if(file == null)
            return;
        delete(file.metadata);
        delete(file.file);
    }
    public void deleteAll(FileWithMetadata... files){
        if(files == null)
            return;
        for(FileWithMetadata file: files) {
            delete(file.metadata);
            delete(file.file);
        }
    }

    @Transaction
    @Query("SELECT * FROM Files WHERE FileId = :fileId")
    public abstract FileWithMetadata get(long fileId);
}
