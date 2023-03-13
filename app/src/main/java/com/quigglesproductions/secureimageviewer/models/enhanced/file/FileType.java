package com.quigglesproductions.secureimageviewer.models.enhanced.file;

public enum FileType {
    UNKNOWN,
    PNG,
    JPG,
    GIF,
    MP4,
    WEBP;



    public static FileType getFileTypeFromExtension(String extension){
        String comparisonString = extension.replace(".","");
        switch (comparisonString.toLowerCase()){
            case "png":
                return PNG;
            case "jpg":
            case "jpeg":
                return JPG;
            case "gif":
                return GIF;
            case "webp":
                return WEBP;
            case "mp4":
                return MP4;
            default:
                return UNKNOWN;
        }
    }

    public boolean hasTransparency(){
        switch (this){
            case GIF:
            case PNG:
            case WEBP:
                return true;
            default:
                return false;
        }
    }
}
