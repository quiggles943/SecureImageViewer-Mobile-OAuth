package com.quigglesproductions.secureimageviewer.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quigglesproductions.secureimageviewer.R;

public class TwoProgressBarDialog extends AlertDialog {
    public ProgressBar topProgressBar, bottomProgressBar;
    private TextView folderName,fileName, topProgress, bottomProgress, alertTitle, alertSubTitle;
    int currentFileCount;
    public TwoProgressBarDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_2bar_progress);
        topProgressBar = findViewById(R.id.folderProgressBar);
        bottomProgressBar = findViewById(R.id.fileProgressBar);
        topProgressBar.setIndeterminate(false);
        bottomProgressBar.setIndeterminate(false);
        folderName = findViewById(R.id.progress_folder_name);
        fileName = findViewById(R.id.progress_file_name);
        topProgress = findViewById(R.id.progress_folder_count);
        bottomProgress = findViewById(R.id.progress_file_count);
        alertTitle = findViewById(R.id.progress_title);
        alertSubTitle = findViewById(R.id.progress_subtitle);
    }
    public void setTopProgressBarTitle(String title){
        folderName.setText(title);
    }
    public void setBottomProgressBarTitle(String title){
        fileName.setText(title);
    }
    public void updateTopProgressBar(int value,int total){
        topProgressBar.setMax(total);
        topProgressBar.setProgress(value);
        topProgress.setText(value+"/"+total);
    }
    public void updateBottomProgressBar(int value,int total){
        bottomProgressBar.setMax(total);
        bottomProgressBar.setProgress(value);
        bottomProgress.setText(value+"/"+total);
    }
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        alertTitle = findViewById(R.id.progress_title);
        alertTitle.setText(title);
    }
    public void setSubTitle(CharSequence subTitle){
        alertSubTitle.setText(subTitle);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
