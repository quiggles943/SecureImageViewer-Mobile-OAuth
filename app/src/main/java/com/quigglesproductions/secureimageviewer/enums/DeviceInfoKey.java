package com.quigglesproductions.secureimageviewer.enums;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public enum DeviceInfoKey {
    DEVICE_REGISTERED(Boolean.class),
    DEVICE_LAST_ONLINE_SYNC(LocalDateTime.class),
    DEVICE_LAST_UPDATE(LocalDateTime.class);

    Type infoType;

    DeviceInfoKey(Type type){
        this.infoType = type;
    }
}
