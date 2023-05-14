package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderListViewModel extends ViewModel {
    private SavedStateHandle state;
    private MutableLiveData<List<EnhancedFolder>> folders;
    public EnhancedFolderListViewModel(SavedStateHandle savedStateHandle){
        state = savedStateHandle;
        folders = new MutableLiveData<>();
    }

    public SavedStateHandle getState() {
        return state;
    }

    public MutableLiveData<List<EnhancedFolder>> getFolders() {
        return folders;
    }
}
