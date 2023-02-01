package com.quigglesproductions.secureimageviewer.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StreamUtilsUnitTest {

    @Test
    public void readStream() throws IOException {
        String testString = "Test String";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        String actualString = StreamUtils.readInputStream(stream);

        assertEquals(testString,actualString);
    }
}
