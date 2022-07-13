package com.quigglesproductions.secureimageviewer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;

    private StreamUtils() {
        throw new IllegalStateException("This type is not intended to be instantiated");
    }

    /**
     * Read a string from an input stream.
     */
    public static String readInputStream(InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("Input stream must not be null");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        char[] buffer = new char[INITIAL_READ_BUFFER_SIZE];
        StringBuilder sb = new StringBuilder();
        int readCount;
        while ((readCount = br.read(buffer)) != -1) {
            sb.append(buffer, 0, readCount);
        }
        return sb.toString();
    }

    /**
     * Close an input stream quietly, i.e. without throwing an exception.
     */
    public static void closeQuietly(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
            // deliberately do nothing
        }
    }
}
