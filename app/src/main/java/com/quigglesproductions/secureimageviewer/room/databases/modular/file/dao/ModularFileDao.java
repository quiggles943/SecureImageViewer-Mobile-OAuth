package com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularArtist;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularCategory;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFile;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFileSubjectCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFolder;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularSubject;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedFile;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedMetadata;
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException;
import com.quigglesproductions.secureimageviewer.room.exceptions.NotInDatabaseException;

import java.util.List;

@Dao
public abstract class ModularFileDao {

    /**
     * Retrieve all files stored within the database
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files")
    public abstract List<RoomEmbeddedFile> getAll();

    /**
     * Retrieves the files identified by the provided array of file ids
     * @param fileIds an array of file ids
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FileId IN (:fileIds)")
    public abstract List<RoomEmbeddedFile> loadAllByIds(int[] fileIds);

    /**
     * Retrieves all the files which are part of the folder identifed by the provided folder id
     * @param folderId the id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId IN (:folderId)")
    public abstract List<RoomEmbeddedFile> loadAllInFolder(int folderId);

    /**
     * Retrieves all the files which are part of the folder identifed by the provided online folder
     * id
     * @param onlineFolderId the online id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE OnlineFolderId IN (:onlineFolderId)")
    public abstract List<RoomEmbeddedFile> loadAllInOnlineFolder(int onlineFolderId);

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
    public long insert(RoomModularFolder folder, RoomEmbeddedFile file)throws DatabaseInsertionException{
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
            if(file.categories != null){
                for(RoomModularCategory category : file.categories){
                    long categoryId = insert(category);
                    RoomModularFileCategoryCrossRef categoryCrossRef = new RoomModularFileCategoryCrossRef();
                    categoryCrossRef.categoryId = categoryId;
                    categoryCrossRef.fileId = fileId;
                    insert(categoryCrossRef);
                }
            }
            if(file.subjects != null)
            {
                for(RoomModularSubject subject : file.subjects){
                    long subjectId = insert(subject);
                    RoomModularFileSubjectCrossRef subjectCrossRef = new RoomModularFileSubjectCrossRef();
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
    public void insertAll(RoomModularFolder folder, RoomEmbeddedFile... files) throws DatabaseInsertionException {
        if(files != null && files.length>0){
            for(RoomEmbeddedFile file : files){
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
                if(file.categories != null){
                    for(RoomModularCategory category : file.categories){
                        long categoryId = insert(category);
                        RoomModularFileCategoryCrossRef categoryCrossRef = new RoomModularFileCategoryCrossRef();
                        categoryCrossRef.categoryId = categoryId;
                        categoryCrossRef.fileId = fileId;
                        insert(categoryCrossRef);
                    }
                }
                if(file.subjects != null)
                {
                    for(RoomModularSubject subject : file.subjects){
                        long subjectId = insert(subject);
                        RoomModularFileSubjectCrossRef subjectCrossRef = new RoomModularFileSubjectCrossRef();
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
    long insert(RoomModularArtist artist){
        RoomModularArtist existingArtist = getArtistByOnlineId(artist.onlineId);
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
    abstract long _insert(RoomModularArtist artist);
    @Query("SELECT * FROM Artists WHERE onlineId = :onlineId")
    abstract RoomModularArtist getArtistByOnlineId(long onlineId);
    @Update
    abstract void _update(RoomModularArtist artist);
    @Insert
    long insert(RoomModularCategory category){
        RoomModularCategory existingCategory = getCategoryByOnlineId(category.onlineId);
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
    abstract long _insert(RoomModularCategory category);
    @Query("SELECT * FROM Categories WHERE onlineId = :onlineId")
    abstract RoomModularCategory getCategoryByOnlineId(long onlineId);
    @Update
    abstract void _update(RoomModularCategory category);
    @Insert
    long insert(RoomModularSubject subject){
        RoomModularSubject existingSubject = getSubjectByOnlineId(subject.onlineId);
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
    abstract long _insert(RoomModularSubject category);
    @Query("SELECT * FROM Subjects WHERE onlineId = :onlineId")
    abstract RoomModularSubject getSubjectByOnlineId(long onlineId);
    @Update
    public abstract void update(RoomModularFile file);
    @Update
    public abstract void update(RoomModularMetadata metadata);
    @Update
    abstract void _update(RoomModularSubject category);
    @Insert
    abstract void insert(RoomModularFileSubjectCrossRef subjectCrossRef);
    @Insert
    abstract void insert(RoomModularFileCategoryCrossRef categoryCrossRef);

    @Delete
    abstract void delete(RoomModularFile file);
    @Delete
    void delete(RoomEmbeddedMetadata metadata){
        if(metadata == null)
            return;


        delete(metadata.metadata);
    }
    @Delete
    abstract void delete(RoomModularMetadata metadata);
    @Query("DELETE FROM FileModularCategoryCrossRef WHERE FileId = :fileId")
    abstract void deleteCategoryReferences(long fileId);
    @Query("DELETE FROM FileModularSubjectCrossRef WHERE FileId = :fileId")
    abstract void deleteSubjectReferences(long fileId);
    @Delete
    abstract void deleteAll(RoomModularFile... files);

    @Insert
    abstract long insert(RoomModularFile file);
    @Insert
    abstract long insert(RoomModularMetadata metadata);
    @Insert
    abstract void insertAll(RoomModularCategory... categories);
    @Insert
    abstract void insertAll(RoomModularSubject... subjects);
    public void delete(RoomEmbeddedFile file){
        if(file == null)
            return;
        deleteCategoryReferences(file.getId());
        deleteSubjectReferences(file.getId());
        if(file.metadata != null && file.metadata.metadata != null)
            delete(file.metadata.metadata);
        delete(file.file);
    }
    public void deleteAll(RoomEmbeddedFile... files){
        if(files == null)
            return;
        for(RoomEmbeddedFile file: files) {
            deleteCategoryReferences(file.getId());
            deleteSubjectReferences(file.getId());
            delete(file.metadata.metadata);
            delete(file.file);
        }
    }

    @Transaction
    @Query("SELECT * FROM Files WHERE FileId = :fileId")
    public abstract RoomEmbeddedFile get(long fileId);
}
