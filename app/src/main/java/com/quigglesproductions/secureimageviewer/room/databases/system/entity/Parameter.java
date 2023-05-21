package com.quigglesproductions.secureimageviewer.room.databases.system.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.quigglesproductions.secureimageviewer.room.Converters;
import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter;

import java.time.LocalDateTime;

@Entity(tableName = "Parameters")
public class Parameter {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "ProcessKey")
    public SystemParameter key;
    @ColumnInfo(name = "ProcessValue")
    public String valueRaw;
    @ColumnInfo(name = "CreationTime")
    public LocalDateTime creationTime;
    @ColumnInfo(name = "UpdateTime")
    public LocalDateTime updateTime;

    @Ignore
    public boolean isSet;

    public LocalDateTime getValueLocalDateTime() {
        if(isSet && valueRaw != null)
            return Converters.fromDateFormat(valueRaw);
        else
            return null;
    }
}
