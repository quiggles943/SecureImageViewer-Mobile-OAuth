package com.quigglesproductions.secureimageviewer.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BooleanUtilsUnitTest {

    @Test
    public void getBoolFromInt(){
        boolean actualBoolFalse = BooleanUtils.getBoolFromInt(0);
        boolean actualBoolTrue = BooleanUtils.getBoolFromInt(1);
        assertFalse(actualBoolFalse);
        assertTrue(actualBoolTrue);
    }

    @Test
    public void getBoolFromString(){
        boolean actualTrue = BooleanUtils.getBoolFromString("true");
        boolean actualTrue2 = BooleanUtils.getBoolFromString("yes");
        boolean actualFalse = BooleanUtils.getBoolFromString("false");
        boolean actualFalse2 = BooleanUtils.getBoolFromString("no");

        assertTrue(actualTrue);
        assertTrue(actualTrue2);
        assertFalse(actualFalse);
        assertFalse(actualFalse2);
    }
}
