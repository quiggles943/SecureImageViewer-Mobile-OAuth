package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

public class EnhancedArtist extends EnhancedFileTag {
    public EnhancedArtist(){
        super();
    }
    public EnhancedArtist(int onlineId,String name){
        super(onlineId,name);
    }
}
