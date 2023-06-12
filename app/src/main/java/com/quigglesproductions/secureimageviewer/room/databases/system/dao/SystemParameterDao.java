package com.quigglesproductions.secureimageviewer.room.databases.system.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.quigglesproductions.secureimageviewer.room.databases.system.entity.Parameter;
import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter;
import com.quigglesproductions.secureimageviewer.room.exceptions.EntityAlreadyExistsException;

import java.time.LocalDateTime;

@Dao
public abstract class SystemParameterDao {

    @Query("SELECT * FROM Parameters WHERE ProcessKey = :key")
    abstract Parameter getParameter(SystemParameter key);
    @Insert
    public long insert(Parameter parameter) throws EntityAlreadyExistsException {
        Parameter savedParameter = getParameterByKey(parameter.key);
        if(savedParameter != null)
            throw new EntityAlreadyExistsException();
        parameter.creationTime = LocalDateTime.now();
        parameter.updateTime = LocalDateTime.now();
        return _insert(parameter);
    }

    @Insert
    abstract long _insert(Parameter parameter);

    public Parameter getParameterByKey(SystemParameter key){
        Parameter parameter = getParameter(key);
        if(parameter == null){
            parameter = new Parameter();
            parameter.key = key;
            parameter.isSet = false;
        }
        else{
            parameter.isSet = true;
        }
        return parameter;
    }
}
