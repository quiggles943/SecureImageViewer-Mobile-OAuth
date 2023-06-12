package com.quigglesproductions.secureimageviewer.models.enhanced.file;

import java.io.File;

public interface IDatabaseFile extends IDisplayFile{
    String getFilePath();
    void setFilePath(String filePath);

    String getThumbnailPath();
    void setThumbnailPath(String thumbnailPath);

    void setImageFile(File file);

    void setThumbnailFile(File thumbnail);

    File getImageFile();
}
