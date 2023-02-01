package com.quigglesproductions.secureimageviewer.utils;

import java.util.ArrayList;

public class ListUtils {

    public static String convertListToDelim(ArrayList<?> lst) {

        StringBuilder sb = new StringBuilder();

        for (Object oVal : lst) {

            String s = oVal.toString();

            if (s.length() > 0) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(s);
            }

        }

        return sb.toString();

    }

    public static String[] convertListToStringArray(ArrayList<?> categoryIds) {
        String[] array = new String[categoryIds.size()];
        int count = 0;
        for(Object oVal : categoryIds){
            array[count] = oVal.toString();
            count++;
        }
        return array;
    }
}
