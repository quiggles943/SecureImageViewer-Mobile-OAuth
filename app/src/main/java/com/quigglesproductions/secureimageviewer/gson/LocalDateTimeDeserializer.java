package com.quigglesproductions.secureimageviewer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer  implements JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        if(!element.isJsonNull()) {
            try {
                return LocalDateTime.parse(element.getAsString());
            } catch (DateTimeParseException ex) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                return LocalDateTime.parse(element.getAsString(), formatter);
            }catch(UnsupportedOperationException exception){
                return null;
            }
        }
        return null;
    }
}
