package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderViewerViewModel extends ViewModel {
    private SavedStateHandle state;
    private MutableLiveData<EnhancedFolder> folder;
    private MutableLiveData<ArrayList<EnhancedFile>> files;
    public EnhancedFolderViewerViewModel(SavedStateHandle savedStateHandle){
        folder = new MutableLiveData<>();
        files = new MutableLiveData<>();
        this.state = savedStateHandle;
    }

    public MutableLiveData<EnhancedFolder> getFolder() {
        return folder;
    }

    public MutableLiveData<ArrayList<EnhancedFile>> getFiles() {
        return files;
    }

    public SavedStateHandle getState() {
        return state;
    }
}
