package com.quigglesproductions.secureimageviewer.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class ChecksumSignature implements Key {
    private final FileChecksum checksum;

    public ChecksumSignature(FileChecksum fileChecksum) {
        this.checksum = fileChecksum;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChecksumSignature) {
            ChecksumSignature other = (ChecksumSignature) o;
            return checksum == other.checksum;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return checksum.hashCode();
    }
    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(checksum.getChecksumString().getBytes(CHARSET));
    }
}
