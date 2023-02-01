package com.quigglesproductions.secureimageviewer;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

    public abstract class GenericClass<T>{
        private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
        private final Type type = typeToken.getType(); // or getRawType() to return Class<? super T>

        public Type getType() {
            return type;
        }
    }
