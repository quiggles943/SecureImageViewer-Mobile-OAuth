package com.quigglesproductions.secureimageviewer.room.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;

import java.util.List;

public class FolderWithFiles {
    @Embedded
    public RoomDatabaseFolder folder;
    @Relation(parentColumn = "FolderId",entityColumn = "FolderId",entity = RoomDatabaseFile.class)
    public List<FileWithMetadata> metadata;
}
