package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;

import java.util.ArrayList;

public class EnhancedFolderViewerViewModel extends ViewModel {
    private SavedStateHandle state;
    private MutableLiveData<EnhancedFolder> folder;
    private MutableLiveData<ArrayList<IDisplayFile>> files;
    public EnhancedFolderViewerViewModel(SavedStateHandle savedStateHandle){
        folder = new MutableLiveData<>();
        files = new MutableLiveData<>();
        this.state = savedStateHandle;
    }

    public MutableLiveData<EnhancedFolder> getFolder() {
        return folder;
    }

    public MutableLiveData<ArrayList<IDisplayFile>> getFiles() {
        return files;
    }

    public SavedStateHandle getState() {
        return state;
    }
}
