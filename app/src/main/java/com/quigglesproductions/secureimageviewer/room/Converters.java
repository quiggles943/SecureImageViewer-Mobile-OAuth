package com.quigglesproductions.secureimageviewer.room;

import androidx.room.TypeConverter;
import androidx.work.WorkInfo;

import com.quigglesproductions.secureimageviewer.checksum.ChecksumAlgorithm;
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.downloader.DownloadState;
import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

    @TypeConverter
    public static IFolderDataSource.FolderSourceType toFolderSourceType(String value){
        if(value != null)
            return IFolderDataSource.FolderSourceType.valueOf(value);
        else
            return IFolderDataSource.FolderSourceType.ONLINE;
    }

    @TypeConverter
    public static String fromFolderSourceType(IFolderDataSource.FolderSourceType folderSourceType){
        return folderSourceType.name();
    }

    @TypeConverter
    public static DownloadState toDownloadState(String value){
        if(value != null)
            return DownloadState.valueOf(value);
        else
            return DownloadState.UNKNOWN;
    }

    @TypeConverter
    public static String fromDownloadState(DownloadState downloadState){
        return downloadState.name();
    }
    @TypeConverter
    public static UUID toUUID(String value){
        return UUID.fromString(value);
    }

    @TypeConverter
    public static String fromUUID(UUID uuid){
        return uuid.toString();
    }
    @TypeConverter
    public static WorkInfo.State toWorkInfoState(String value){
        if(value != null && !value.isBlank())
            return WorkInfo.State.valueOf(value);
        else
            return null;
    }

    @TypeConverter
    public static String fromWorkInfoState(WorkInfo.State state){
        if(state != null)
            return state.name();
        else
            return "";
    }

    @TypeConverter
    public static ChecksumAlgorithm toChecksumAlgorithm(String value){
        if(value != null)
            return ChecksumAlgorithm.valueOf(value);
        else
            return ChecksumAlgorithm.UNKNOWN;
    }

    @TypeConverter
    public static String fromChecksumAlgorithm(ChecksumAlgorithm checksumAlgorithm){
        return checksumAlgorithm.name();
    }
}
