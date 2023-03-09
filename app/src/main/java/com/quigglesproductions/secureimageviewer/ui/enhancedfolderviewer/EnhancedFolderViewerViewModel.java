package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderViewerViewModel extends ViewModel {
    private MutableLiveData<EnhancedFolder> folder;
    private MutableLiveData<ArrayList<EnhancedFile>> files;
    public EnhancedFolderViewerViewModel(){
        folder = new MutableLiveData<>();
        files = new MutableLiveData<>();
    }

    public MutableLiveData<EnhancedFolder> getFolder() {
        return folder;
    }

    public MutableLiveData<ArrayList<EnhancedFile>> getFiles() {
        return files;
    }
}
