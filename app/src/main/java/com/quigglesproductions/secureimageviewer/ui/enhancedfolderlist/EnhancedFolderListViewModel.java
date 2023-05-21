package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderListViewModel extends ViewModel {
    private SavedStateHandle state;
    private MutableLiveData<List<IDisplayFolder>> folders;
    public EnhancedFolderListViewModel(SavedStateHandle savedStateHandle){
        state = savedStateHandle;
        folders = new MutableLiveData<>();
    }

    public SavedStateHandle getState() {
        return state;
    }

    public MutableLiveData<List<IDisplayFolder>> getFolders() {
        return folders;
    }
}
