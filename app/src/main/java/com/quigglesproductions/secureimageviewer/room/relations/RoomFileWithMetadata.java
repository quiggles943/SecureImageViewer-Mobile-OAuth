package com.quigglesproductions.secureimageviewer.room.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;

public class RoomFileWithMetadata {
    @Embedded
    public RoomDatabaseFile file;
    @Relation(parentColumn = "FileId",entityColumn = "FileId",entity = RoomFileMetadata.class)
    public RoomFileMetadataWithEntities metadata;
    public RoomFileWithMetadata() {}
}
