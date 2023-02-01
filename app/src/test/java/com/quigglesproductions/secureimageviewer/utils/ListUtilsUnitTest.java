package com.quigglesproductions.secureimageviewer.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.ArrayList;

public class ListUtilsUnitTest {

    @Test
    public void convertListToDelineatedString(){
        ArrayList<Integer> lstInt = new ArrayList<>();
        lstInt.add(1);
        lstInt.add(2);
        lstInt.add(3);
        lstInt.add(4);

        String testString = ListUtils.convertListToDelim(lstInt);
        assertEquals("1,2,3,4",testString);
    }

    @Test
    public void convertListToStringArray(){
        ArrayList<Integer> lstInt = new ArrayList<>();
        lstInt.add(1);
        lstInt.add(2);
        lstInt.add(3);
        lstInt.add(4);
        String[] expectedStringArray = new String[4];
        expectedStringArray[0] = "1";
        expectedStringArray[1] = "2";
        expectedStringArray[2] = "3";
        expectedStringArray[3] = "4";
        String[] testStringArray = ListUtils.convertListToStringArray(lstInt);
        assertArrayEquals(expectedStringArray,testStringArray);
    }
}
