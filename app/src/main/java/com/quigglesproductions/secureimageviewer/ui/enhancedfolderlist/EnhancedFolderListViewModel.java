package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderListViewModel extends ViewModel {
    private MutableLiveData<List<EnhancedFolder>> folders;
    public EnhancedFolderListViewModel(){
        folders = new MutableLiveData<>();
    }

    public MutableLiveData<List<EnhancedFolder>> getFolders() {
        return folders;
    }
}
