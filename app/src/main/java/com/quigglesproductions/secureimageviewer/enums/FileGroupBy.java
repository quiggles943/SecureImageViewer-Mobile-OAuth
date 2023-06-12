package com.quigglesproductions.secureimageviewer.enums;

public enum FileGroupBy {
    UNKNOWN(""),
    FOLDERS("Folders"),
    CATEGORIES("Categories"),
    SUBJECTS("Subjects");

    private String name;
    FileGroupBy(String name){
        this.name = name;
    }

    public static FileGroupBy fromDisplayName(String resultString) {
        for(FileGroupBy fileGroupBy : FileGroupBy.values()){
            if(fileGroupBy.name().equalsIgnoreCase(resultString))
                return fileGroupBy;
        }
        return UNKNOWN;
    }

    public String getDisplayName() {
        return name;
    }
}
