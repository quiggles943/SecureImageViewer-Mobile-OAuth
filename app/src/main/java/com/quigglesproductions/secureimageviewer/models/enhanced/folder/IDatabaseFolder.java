package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import java.io.File;
import java.time.LocalDateTime;

public interface IDatabaseFolder {
    long getId();

    File getThumbnailFile();

    LocalDateTime getDownloadTime();
}
