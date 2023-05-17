package com.quigglesproductions.secureimageviewer.room;

import androidx.room.TypeConverter;

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
}
