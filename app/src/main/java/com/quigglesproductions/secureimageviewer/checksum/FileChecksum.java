package com.quigglesproductions.secureimageviewer.checksum;

public class FileChecksum {
    String checksumString;
    ChecksumAlgorithm checksumAlgorithm;
    public FileChecksum(String checksum, ChecksumAlgorithm type){
        this.checksumString = checksum;
        this.checksumAlgorithm = type;
    }

    public FileChecksum(String checksum, String type){
        this.checksumString = checksum;
        this.checksumAlgorithm = ChecksumAlgorithm.getAlgorithmFromString(type);
    }

    public String getChecksumString(){
        return checksumString;
    }

    public ChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }
}
