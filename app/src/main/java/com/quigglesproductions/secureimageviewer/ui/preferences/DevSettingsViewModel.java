package com.quigglesproductions.secureimageviewer.ui.preferences;

import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.retrofit.RequestService;
import com.quigglesproductions.secureimageviewer.ui.data.LoginRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DevSettingsViewModel extends ViewModel {
    @Inject
    public DevSettingsViewModel() {
    }
}
