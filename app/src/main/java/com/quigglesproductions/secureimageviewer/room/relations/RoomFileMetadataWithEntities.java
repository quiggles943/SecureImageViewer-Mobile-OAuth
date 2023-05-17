package com.quigglesproductions.secureimageviewer.room.relations;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileSubjectCrossRef;

import java.util.List;

@Entity
public class RoomFileMetadataWithEntities {
    @Embedded
    public RoomFileMetadata metadata;
    @Relation(parentColumn = "FileId",entityColumn = "SubjectId",associateBy = @Junction(RoomFileSubjectCrossRef.class))
    public List<RoomDatabaseSubject> subjects;
    @Relation(parentColumn = "FileId",entityColumn = "CategoryId",associateBy = @Junction(RoomFileCategoryCrossRef.class))
    public List<RoomDatabaseCategory> categories;
    @Relation(parentColumn = "ArtistId",entityColumn = "uid")
    public RoomDatabaseArtist artist;
    public RoomFileMetadataWithEntities(){}
}
