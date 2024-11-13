package com.quigglesproductions.secureimageviewer.glide

import com.bumptech.glide.load.Key
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum
import java.security.MessageDigest

class ChecksumSignature(private val checksum: FileChecksum) : Key {
    override fun equals(other: Any?): Boolean {
        if (other is ChecksumSignature) {
            return checksum === other.checksum
        }
        return false
    }

    override fun hashCode(): Int {
        return checksum.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(checksum.checksumString.toByteArray(Key.CHARSET))
    }
}
