package com.quigglesproductions.secureimageviewer;

public enum SortType {
    NAME_ASC,
    NAME_DESC,
    NEWEST_FIRST,
    OLDEST_FIRST;

    public static SortType getFromName(String name){
        SortType result = NAME_ASC;
        for (SortType sort : SortType.values()){
            if(sort.name().equalsIgnoreCase(name)){
                result = sort;
            }
        }
        return result;
    }
}
