package com.quigglesproductions.secureimageviewer.checksum;

public enum ChecksumAlgorithm {
    UNKNOWN,
    SHA1,
    SHA256,
    SHA384,
    SHA512,
    MD5,
    MD256;

    public static ChecksumAlgorithm getAlgorithmFromString(String value){
        for(ChecksumAlgorithm algorithm : ChecksumAlgorithm.values()){
            if(algorithm.name().equalsIgnoreCase(value))
                return algorithm;
        }
        return UNKNOWN;
    }
}
