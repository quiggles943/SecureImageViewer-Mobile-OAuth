package com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFaceScanLandmark;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFaceScanModel;

import java.util.List;

public class RoomUnifiedEmbeddedFaceScanModel {
    @Embedded
    public RoomUnifiedFaceScanModel model;
    @Relation(parentColumn = "FaceScanId",entityColumn = "FaceScanModelId",entity = RoomUnifiedFaceScanLandmark.class)
    public List<RoomUnifiedFaceScanLandmark> landmarks;
}
