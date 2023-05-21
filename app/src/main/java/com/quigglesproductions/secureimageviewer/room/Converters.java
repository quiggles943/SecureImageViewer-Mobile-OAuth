package com.quigglesproductions.secureimageviewer.room;

import androidx.room.TypeConverter;

import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Converters {
    @TypeConverter
    public static LocalDateTime fromDateFormat(String value){
        if(value != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            return LocalDateTime.parse(value, formatter);
        }
        return null;
    }
    @TypeConverter
    public static String dateToString(LocalDateTime date){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        if(date != null)
            return date.atOffset(ZoneOffset.UTC).format(formatter);
        else
            return null;
    }

    @TypeConverter
    public static SystemParameter toSystemParameter(String value){
        if(value != null){
            return SystemParameter.getFromKey(value);
        }
        return SystemParameter.UNKNOWN;
    }

    @TypeConverter
    public static String fromSystemParameter(SystemParameter systemParameter){
        return systemParameter.name();
    }
}
