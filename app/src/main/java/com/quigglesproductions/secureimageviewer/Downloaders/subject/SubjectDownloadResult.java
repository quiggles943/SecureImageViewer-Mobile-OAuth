package com.quigglesproductions.secureimageviewer.Downloaders.subject;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.models.SubjectModel;

import java.util.ArrayList;

public class SubjectDownloadResult {
    public static int TOKEN_EXPIRED = 1;
    @Nullable
    private ArrayList<SubjectModel> success;
    @Nullable
    private Integer error;
    private boolean isSuccessful;
    public SubjectDownloadResult(@Nullable Integer error) {
        this.error = error;
        this.isSuccessful = false;
    }

    public SubjectDownloadResult(@Nullable ArrayList<SubjectModel> success) {
        this.success = success;
        isSuccessful = true;
    }

    public boolean getIsSuccessful(){
        return isSuccessful;
    }
    @Nullable
    public ArrayList<SubjectModel> getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}
